const express = require('express');
const cors = require('cors');
const authRoutes = require('./routes/authRoutes'); 
const studentRoutes = require('./routes/studentRoutes');
const facultyRoutes = require('./routes/facultyRoutes');
const adminRoutes = require('./routes/adminRoutes');

const app = express();
app.use(cors());
app.use(express.json());

app.use('/api', authRoutes); // ✅ Add this before anything else

app.use('/api', adminRoutes);
app.use('/api/faculty/', facultyRoutes);
app.use('/api/student', studentRoutes);

const PORT = process.env.PORT || 4000;
app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
});
