# 🚀 College ERP - Production Deployment Guide

## Overview
This guide will help you deploy the College ERP system to a production environment.

## Prerequisites

### Required
- **Server**: Linux server (Ubuntu 20.04+ recommended)
- **Java**: OpenJDK 21
- **Database**: PostgreSQL 14+
- **Memory**: Minimum 2GB RAM
- **Storage**: Minimum 10GB free space
- **Domain**: Optional but recommended

### Recommended
- **Reverse Proxy**: Nginx or Apache
- **SSL Certificate**: Let's Encrypt
- **Process Manager**: systemd
- **Monitoring**: Prometheus + Grafana

## Deployment Options

### Option 1: Traditional Server Deployment (Recommended for Learning)
### Option 2: Docker Deployment (Recommended for Production)
### Option 3: Cloud Deployment (AWS, Azure, Google Cloud)

---

## Option 1: Traditional Server Deployment

### Step 1: Server Setup

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Java 21
sudo apt install openjdk-21-jdk -y

# Verify installation
java -version  # Should show 21.x.x

# Install PostgreSQL
sudo apt install postgresql postgresql-contrib -y

# Install Nginx (optional, for reverse proxy)
sudo apt install nginx -y
```

### Step 2: Database Setup

```bash
# Switch to postgres user
sudo -u postgres psql

# In PostgreSQL prompt:
CREATE DATABASE college_erp;
CREATE USER erp_user WITH ENCRYPTED PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE college_erp TO erp_user;
\q
```

### Step 3: Backend Deployment

```bash
# Create application directory
sudo mkdir -p /opt/college-erp
sudo chown $USER:$USER /opt/college-erp

# Upload your backend code
# Method 1: Git clone
cd /opt/college-erp
git clone your-repository-url backend

# Method 2: SCP from local machine
# scp -r /path/to/backend user@server:/opt/college-erp/

# Navigate to backend
cd /opt/college-erp/backend/backend

# Update application.properties for production
nano src/main/resources/application.properties
```

**Production application.properties:**
```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/college_erp
spring.datasource.username=erp_user
spring.datasource.password=your_secure_password

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# Server Configuration
server.port=8080
server.address=127.0.0.1  # Only allow localhost, Nginx will proxy

# Security
jwt.secret=your-very-long-and-secure-jwt-secret-key-here-at-least-512-bits
jwt.expiration=86400000  # 24 hours

# Logging
logging.level.root=INFO
logging.level.com.college.erp=INFO
logging.file.name=/var/log/college-erp/application.log

# Production optimizations
spring.jpa.open-in-view=false
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

```bash
# Build the application
./mvnw clean package -DskipTests

# The JAR file will be in target/
ls -lh target/*.jar
```

### Step 4: Create systemd Service

```bash
# Create service file
sudo nano /etc/systemd/system/college-erp.service
```

**Service file content:**
```ini
[Unit]
Description=College ERP Backend
After=postgresql.service
Requires=postgresql.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/college-erp/backend/backend
ExecStart=/usr/bin/java -jar /opt/college-erp/backend/backend/target/your-app-name.jar
Restart=on-failure
RestartSec=10

# Environment variables
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="JAVA_OPTS=-Xmx1024m -Xms512m"

# Logging
StandardOutput=journal
StandardError=journal
SyslogIdentifier=college-erp

[Install]
WantedBy=multi-user.target
```

```bash
# Create log directory
sudo mkdir -p /var/log/college-erp
sudo chown www-data:www-data /var/log/college-erp

# Reload systemd, enable and start service
sudo systemctl daemon-reload
sudo systemctl enable college-erp
sudo systemctl start college-erp

# Check status
sudo systemctl status college-erp

# View logs
sudo journalctl -u college-erp -f
```

### Step 5: Frontend Deployment

```bash
# Create frontend directory
sudo mkdir -p /var/www/college-erp
sudo chown $USER:$USER /var/www/college-erp

# Upload frontend files
# scp -r /path/to/github1-master/* user@server:/var/www/college-erp/

# Update api-config.js for production
nano /var/www/college-erp/api-config.js
```

**Production api-config.js:**
```javascript
// Production API configuration
const API_BASE_URL = 'https://your-domain.com/api';  // Update with your domain

// ... rest of the file stays the same
```

### Step 6: Nginx Configuration

```bash
# Create Nginx configuration
sudo nano /etc/nginx/sites-available/college-erp
```

**Nginx configuration:**
```nginx
# Backend upstream
upstream backend {
    server 127.0.0.1:8080;
}

# HTTP server (redirect to HTTPS)
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;
    return 301 https://$server_name$request_uri;
}

# HTTPS server
server {
    listen 443 ssl http2;
    server_name your-domain.com www.your-domain.com;

    # SSL certificates (get from Let's Encrypt)
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    
    # SSL configuration
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # Frontend files
    root /var/www/college-erp;
    index index.html system-status.html;

    # Gzip compression
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;

    # Frontend routes
    location / {
        try_files $uri $uri/ =404;
    }

    # API proxy
    location /api/ {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket support (if needed later)
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;
}
```

```bash
# Enable site
sudo ln -s /etc/nginx/sites-available/college-erp /etc/nginx/sites-enabled/

# Test configuration
sudo nginx -t

# Reload Nginx
sudo systemctl reload nginx
```

### Step 7: SSL Certificate (Let's Encrypt)

```bash
# Install Certbot
sudo apt install certbot python3-certbot-nginx -y

# Obtain certificate
sudo certbot --nginx -d your-domain.com -d www.your-domain.com

# Test auto-renewal
sudo certbot renew --dry-run
```

### Step 8: Firewall Configuration

```bash
# Allow SSH, HTTP, HTTPS
sudo ufw allow OpenSSH
sudo ufw allow 'Nginx Full'
sudo ufw enable

# Check status
sudo ufw status
```

### Step 9: Initial Data Setup

```bash
# Access add-sample-data.html
# https://your-domain.com/add-sample-data.html

# Or use API directly
curl -X POST https://your-domain.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin User",
    "email": "admin@your-domain.com",
    "password": "secure_password",
    "role": "ADMIN"
  }'
```

---

## Option 2: Docker Deployment

### Step 1: Create Dockerfile for Backend

```dockerfile
# backend/Dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Build application
RUN ./mvnw package -DskipTests

# Expose port
EXPOSE 8080

# Run application
CMD ["java", "-jar", "target/college-erp-backend.jar"]
```

### Step 2: Create docker-compose.yml

```yaml
version: '3.8'

services:
  # PostgreSQL Database
  db:
    image: postgres:14-alpine
    container_name: college-erp-db
    environment:
      POSTGRES_DB: college_erp
      POSTGRES_USER: erp_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - erp-network
    restart: unless-stopped

  # Backend Application
  backend:
    build:
      context: ./backend/backend
      dockerfile: Dockerfile
    container_name: college-erp-backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/college_erp
      SPRING_DATASOURCE_USERNAME: erp_user
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
    ports:
      - "8080:8080"
    depends_on:
      - db
    networks:
      - erp-network
    restart: unless-stopped

  # Nginx for Frontend
  frontend:
    image: nginx:alpine
    container_name: college-erp-frontend
    volumes:
      - ./github1-master:/usr/share/nginx/html:ro
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
    ports:
      - "80:80"
      - "443:443"
    depends_on:
      - backend
    networks:
      - erp-network
    restart: unless-stopped

volumes:
  postgres_data:

networks:
  erp-network:
    driver: bridge
```

### Step 3: Environment Variables

```bash
# Create .env file
nano .env
```

```env
DB_PASSWORD=your_secure_db_password
JWT_SECRET=your-very-long-and-secure-jwt-secret-key
```

### Step 4: Deploy with Docker

```bash
# Build and start all services
docker-compose up -d

# Check logs
docker-compose logs -f

# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: deletes data)
docker-compose down -v
```

---

## Post-Deployment Checklist

### Security
- [ ] Change all default passwords
- [ ] Update JWT secret to strong value
- [ ] Enable HTTPS/SSL
- [ ] Configure firewall
- [ ] Set up fail2ban for SSH protection
- [ ] Regular security updates

### Monitoring
- [ ] Set up application logging
- [ ] Configure log rotation
- [ ] Set up monitoring (Prometheus/Grafana)
- [ ] Configure uptime monitoring
- [ ] Set up error alerting

### Backup
- [ ] Configure database backups
- [ ] Test backup restoration
- [ ] Set up off-site backup storage
- [ ] Document backup procedures

### Performance
- [ ] Enable caching (Redis/Memcached)
- [ ] Configure CDN for static assets
- [ ] Optimize database queries
- [ ] Set up connection pooling

### Maintenance
- [ ] Document deployment process
- [ ] Create runbooks for common issues
- [ ] Set up CI/CD pipeline
- [ ] Schedule regular updates

---

## Useful Commands

### Backend Management
```bash
# Restart backend
sudo systemctl restart college-erp

# View logs
sudo journalctl -u college-erp -f

# Check status
sudo systemctl status college-erp
```

### Database Management
```bash
# Backup database
pg_dump -U erp_user college_erp > backup_$(date +%Y%m%d).sql

# Restore database
psql -U erp_user college_erp < backup_20241024.sql

# Connect to database
sudo -u postgres psql college_erp
```

### Nginx Management
```bash
# Test configuration
sudo nginx -t

# Reload configuration
sudo systemctl reload nginx

# View access logs
sudo tail -f /var/log/nginx/access.log

# View error logs
sudo tail -f /var/log/nginx/error.log
```

---

## Troubleshooting

### Backend won't start
```bash
# Check logs
sudo journalctl -u college-erp -n 100

# Check if port is in use
sudo lsof -i :8080

# Verify Java version
java -version
```

### Database connection errors
```bash
# Check PostgreSQL status
sudo systemctl status postgresql

# Check database exists
sudo -u postgres psql -l | grep college_erp

# Test connection
psql -U erp_user -h localhost -d college_erp
```

### Nginx issues
```bash
# Check configuration syntax
sudo nginx -t

# Check if Nginx is running
sudo systemctl status nginx

# Check error logs
sudo tail -100 /var/log/nginx/error.log
```

---

## Performance Optimization

### Database
```sql
-- Add indexes for frequently queried columns
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_students_admission_number ON students(admission_number);
CREATE INDEX idx_enrollments_student_id ON enrollments(student_id);

-- Analyze tables
ANALYZE users;
ANALYZE students;
ANALYZE faculty;
```

### Application
```properties
# In application.properties

# Connection pooling
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000

# JPA optimizations
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

### Nginx
```nginx
# Enable caching
location ~* \.(jpg|jpeg|png|gif|ico|css|js)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
}

# Enable gzip
gzip on;
gzip_vary on;
gzip_types text/plain text/css application/json application/javascript;
```

---

## Monitoring Setup

### Basic Health Check Endpoint

Add to Spring Boot:
```java
@RestController
@RequestMapping("/actuator")
public class HealthController {
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
```

### Uptime Monitoring
Use services like:
- UptimeRobot (free)
- Pingdom
- StatusCake

Configure to check: `https://your-domain.com/actuator/health`

---

## Maintenance Schedule

### Daily
- Check error logs
- Monitor disk space
- Verify backups completed

### Weekly
- Review application logs
- Check security updates
- Monitor performance metrics

### Monthly
- Update dependencies
- Review and optimize database
- Test disaster recovery procedures
- Security audit

### Quarterly
- Full system audit
- Performance review
- Capacity planning
- Documentation update

---

## Success Metrics

After deployment, monitor:
- **Uptime**: Target 99.9%
- **Response Time**: < 500ms average
- **Error Rate**: < 0.1%
- **Database Performance**: Queries < 100ms
- **User Satisfaction**: Collect feedback

---

**Deployment Status**: Ready for production
**Estimated Setup Time**: 2-4 hours
**Difficulty**: Intermediate
**Support**: Check documentation and logs
