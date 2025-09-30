const express = require('express');
const router = express.Router();
const facultyController = require('../controllers/facultyController');

router.post('/', facultyController.createFaculty);
router.get('/', facultyController.getAllFaculty);
router.put('/:id', facultyController.updateFaculty);
router.delete('/:id', facultyController.deleteFaculty);

router.post('/:facultyId/announcements', facultyController.createAnnouncement);
router.put('/:facultyId/announcements/:announcementId', facultyController.updateAnnouncement);
router.delete('/:facultyId/announcements/:announcementId', facultyController.deleteAnnouncement);
// Faculty Education route
router.post('/education', facultyController.createFacultyEducation); 
// Faculty Experience Route
router.post('/experience', facultyController.createFacultyExperience);

// Faculty Acadamic
router.post('/academic-details', facultyController.createFacultyAcademicDetails);

module.exports = router;
