# 🧪 College ERP - Complete Testing Checklist

## Pre-Testing Setup

### ✅ Backend Status
- [ ] Backend running on http://localhost:8080
- [ ] PostgreSQL database online
- [ ] No errors in backend console
- [ ] Health check passes

### ✅ Frontend Status
- [ ] Frontend accessible (http://localhost:3000 or your port)
- [ ] All files loading without 404 errors
- [ ] No console errors on page load

## Test Suite 1: Authentication & Authorization

### Login Tests
- [ ] **Test 1.1**: Login with admin credentials (admin@college.edu / admin123)
  - Expected: Redirect to admin.html
  - Verify: No errors in console, JWT token in localStorage
  
- [ ] **Test 1.2**: Login with student credentials (after adding sample data)
  - Expected: Redirect to student.html
  - Verify: Student dashboard loads with data
  
- [ ] **Test 1.3**: Login with faculty credentials (after adding sample data)
  - Expected: Redirect to faculty.html
  - Verify: Faculty page loads
  
- [ ] **Test 1.4**: Login with wrong password
  - Expected: Error message displayed
  - Verify: No redirect, stays on login page

- [ ] **Test 1.5**: Try accessing admin.html without login
  - Expected: Redirect to login.html
  - Verify: Authentication check working

### Logout Tests
- [ ] **Test 1.6**: Logout from admin dashboard
  - Expected: Redirect to login.html, token cleared
  - Verify: localStorage empty, can't access admin.html

- [ ] **Test 1.7**: Logout from student dashboard
  - Expected: Redirect to login.html, session cleared
  - Verify: Need to login again to access student.html

## Test Suite 2: Sample Data Management

### Add Sample Data
- [ ] **Test 2.1**: Open add-sample-data.html
  - Expected: Page loads with buttons
  - Verify: No console errors

- [ ] **Test 2.2**: Click "Add Sample Students Only"
  - Expected: See success messages for each student
  - Verify: Log shows 8 students added

- [ ] **Test 2.3**: Click "Add Sample Faculty Only"
  - Expected: See success messages for each faculty
  - Verify: Log shows 6 faculty members added

- [ ] **Test 2.4**: Try adding same data again
  - Expected: Errors (duplicate email/ID)
  - Verify: Error messages displayed

## Test Suite 3: Admin Dashboard - Students

### View Students
- [ ] **Test 3.1**: Navigate to Students section
  - Expected: Table shows all students
  - Verify: 8 columns, data visible, no "N/A" for populated fields

- [ ] **Test 3.2**: Check student count in dashboard stats
  - Expected: Count matches number of rows
  - Verify: Real-time update

### Create Student
- [ ] **Test 3.3**: Fill student form and submit
  - Test Data:
    - Roll: 24UCS102
    - Name: Test Student
    - Email: test.student@xaviers.edu
    - Course: Computer Science
    - Phone: 9999999999
    - DOB: 2006-05-15
    - Gender: Male
    - Year: 1
  - Expected: Success message, student appears in table
  - Verify: Backend API called (check Network tab), data persists after refresh

- [ ] **Test 3.4**: Try creating duplicate student
  - Expected: Error message
  - Verify: Student not created

### View Student Details
- [ ] **Test 3.5**: Click "Details" button for any student
  - Expected: Modal opens with complete info
  - Verify: All fields populated correctly, modal can be closed

### Edit Student
- [ ] **Test 3.6**: Click "Edit" button for a student
  - Expected: Edit modal opens with current data
  - Verify: All fields pre-filled

- [ ] **Test 3.7**: Modify student data and save
  - Change: Update phone number to 8888888888
  - Expected: Success message, table updates
  - Verify: Data persists after refresh, backend API called

### Delete Student
- [ ] **Test 3.8**: Click "Remove" button for test student
  - Expected: Confirmation dialog appears
  - Verify: Clicking Cancel doesn't delete

- [ ] **Test 3.9**: Confirm deletion
  - Expected: Success message, student removed from table
  - Verify: Count decreases, data deleted from database (refresh check)

## Test Suite 4: Admin Dashboard - Faculty

### View Faculty
- [ ] **Test 4.1**: Navigate to Faculty section
  - Expected: Table shows all faculty members
  - Verify: 7 columns, proper data display

- [ ] **Test 4.2**: Check faculty count in dashboard stats
  - Expected: Count matches table rows
  - Verify: Matches database count

### Create Faculty
- [ ] **Test 4.3**: Fill faculty form and submit
  - Test Data:
    - ID: F107
    - Name: Test Faculty
    - Email: test.faculty@xaviers.edu
    - Phone: 8888888888
    - DOB: 1985-03-20
    - Gender: Male
  - Expected: Success message, faculty appears in table
  - Verify: Backend API called, data persists

- [ ] **Test 4.4**: Try creating duplicate faculty
  - Expected: Error message
  - Verify: Not created

### View Faculty Details
- [ ] **Test 4.5**: Click "Details" for any faculty
  - Expected: Modal shows complete info
  - Verify: Department, qualification, all fields visible

### Edit Faculty
- [ ] **Test 4.6**: Click "Edit" for a faculty member
  - Expected: Edit modal with current data
  - Verify: Can modify all fields

- [ ] **Test 4.7**: Update faculty and save
  - Change: Department to "IT"
  - Expected: Success, table updates
  - Verify: Persists after refresh

### Delete Faculty
- [ ] **Test 4.8**: Delete test faculty member
  - Expected: Confirmation, successful deletion
  - Verify: Removed from database

## Test Suite 5: Admin Dashboard - Courses

### Course Management
- [ ] **Test 5.1**: Navigate to Courses section
  - Expected: Faculty dropdown populated
  - Verify: All faculty appear in dropdown

- [ ] **Test 5.2**: Assign faculty to course
  - Select: Any faculty, Any course
  - Expected: Assignment appears in list
  - Verify: Course count increases

- [ ] **Test 5.3**: View assignments
  - Expected: List shows faculty → course mapping
  - Verify: Correct names and courses

## Test Suite 6: Admin Dashboard - Reports

### Report Generation
- [ ] **Test 6.1**: Click "Attendance Report"
  - Expected: Table with attendance data
  - Verify: Sample data displayed correctly

- [ ] **Test 6.2**: Click "Marks Report"
  - Expected: Table with marks data
  - Verify: Sample data for subjects shown

## Test Suite 7: Student Dashboard

### Login as Student
- [ ] **Test 7.1**: Logout, login as varsha.g@students.xaviers.edu
  - Expected: Redirect to student.html
  - Verify: Welcome message with name

### View Profile
- [ ] **Test 7.2**: Check profile section
  - Expected: Student details visible
  - Verify: Name, email, course, year shown

### View Enrollments
- [ ] **Test 7.3**: Check course enrollments
  - Expected: List of enrolled courses
  - Verify: Course names and details visible

### View Grades
- [ ] **Test 7.4**: Check grades section
  - Expected: Grades per subject
  - Verify: Course names and marks shown

### View Attendance
- [ ] **Test 7.5**: Check attendance section
  - Expected: Attendance records
  - Verify: Dates and status visible

## Test Suite 8: System Status Page

### Status Dashboard
- [ ] **Test 8.1**: Open system-status.html
  - Expected: Professional dashboard loads
  - Verify: All cards visible

- [ ] **Test 8.2**: Check system status
  - Expected: Green "Online" status for backend
  - Verify: Stats show student/faculty counts

- [ ] **Test 8.3**: Test quick access buttons
  - Expected: All buttons link to correct pages
  - Verify: No 404 errors

## Test Suite 9: Responsive Design

### Desktop (1920x1080)
- [ ] **Test 9.1**: Test all pages on desktop
  - Expected: Full layout, sidebar visible
  - Verify: No overflow, proper spacing

### Tablet (768x1024)
- [ ] **Test 9.2**: Resize to tablet size
  - Expected: Responsive layout
  - Verify: Tables scroll, buttons stack properly

### Mobile (375x667)
- [ ] **Test 9.3**: Resize to mobile size
  - Expected: Mobile-optimized layout
  - Verify: Navigation works, forms usable

## Test Suite 10: Error Handling

### Network Errors
- [ ] **Test 10.1**: Stop backend, try to load students
  - Expected: Error message displayed
  - Verify: User-friendly message, no crash

- [ ] **Test 10.2**: Submit form with backend down
  - Expected: Network error message
  - Verify: Form doesn't freeze

### Validation Errors
- [ ] **Test 10.3**: Submit student form with missing fields
  - Expected: HTML5 validation or API error
  - Verify: Helpful error messages

- [ ] **Test 10.4**: Submit invalid email format
  - Expected: Validation error
  - Verify: User can correct and resubmit

## Test Suite 11: Performance

### Load Time
- [ ] **Test 11.1**: Measure page load time
  - Expected: < 3 seconds
  - Verify: No blocking resources

### API Response Time
- [ ] **Test 11.2**: Check Network tab for API calls
  - Expected: < 500ms average
  - Verify: No slow queries

### Data Refresh
- [ ] **Test 11.3**: Add student, check auto-refresh
  - Expected: Instant update
  - Verify: No manual refresh needed

## Test Suite 12: Security

### Access Control
- [ ] **Test 12.1**: Try accessing admin.html as student
  - Expected: Access denied or redirect
  - Verify: Security check working

- [ ] **Test 12.2**: Manually delete localStorage token
  - Expected: Redirect to login on next action
  - Verify: No unauthorized access

### Password Security
- [ ] **Test 12.3**: Check database for passwords
  - Expected: BCrypt hashes only
  - Verify: No plain text passwords

### JWT Validation
- [ ] **Test 12.4**: Modify JWT token in localStorage
  - Expected: Invalid token error
  - Verify: Backend rejects tampered tokens

## Final Verification

### Code Quality
- [ ] **Final 1**: No console errors on any page
- [ ] **Final 2**: All API calls succeed
- [ ] **Final 3**: No broken links or 404s
- [ ] **Final 4**: All forms validate properly
- [ ] **Final 5**: Logout works from all pages

### Data Integrity
- [ ] **Final 6**: All CRUD operations persist to database
- [ ] **Final 7**: Refresh doesn't lose data
- [ ] **Final 8**: Counts match actual records
- [ ] **Final 9**: Foreign key relationships intact

### User Experience
- [ ] **Final 10**: All buttons responsive
- [ ] **Final 11**: Loading states visible
- [ ] **Final 12**: Success/error messages clear
- [ ] **Final 13**: Navigation intuitive
- [ ] **Final 14**: Forms easy to use

---

## Testing Summary Template

```
TEST DATE: _______________
TESTER: __________________

Total Tests: 100+
Tests Passed: _____
Tests Failed: _____
Pass Rate: _____%

CRITICAL ISSUES:
1. 
2. 
3. 

MINOR ISSUES:
1. 
2. 
3. 

RECOMMENDATIONS:
1. 
2. 
3. 

OVERALL STATUS: ☐ PASS  ☐ FAIL  ☐ NEEDS WORK
```

---

## Quick Test (5 minutes)

If you only have 5 minutes, test these critical flows:

1. ✅ Login as admin
2. ✅ Add new student via form
3. ✅ Edit the student
4. ✅ Delete the student
5. ✅ Add new faculty
6. ✅ Logout and login as student
7. ✅ View student dashboard sections
8. ✅ Logout completely

If all these pass → **System is working!** 🎉

---

**Testing Status**: Ready for comprehensive testing
**Estimated Time**: 2-3 hours for complete testing
**Required**: Backend running, PostgreSQL online, Sample data loaded
