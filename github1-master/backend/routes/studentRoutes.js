const express = require('express');
const router = express.Router();
const studentController = require('../controllers/studentController');

router.post('/', studentController.createStudent);
router.get('/', studentController.getAllStudents);
router.get('/:id', studentController.getOneStudents);
router.put('/:id', studentController.updateStudent);
router.delete('/:id', studentController.deleteStudent);

// Student-Course Mapping Routes
router.post('/assign-course', studentController.assignCourseToStudent); // CREATE
router.get('/courses/:studentId', studentController.getCoursesForStudent); // READ
router.put('/courses/:studentId/:courseId', studentController.updateStudentCourse); // UPDATE
router.delete('/courses/:studentId/:courseId', studentController.removeCourseFromStudent); // DELETE

router.get('/:studentId/announcements', studentController.viewAnnouncements);

module.exports = router;
