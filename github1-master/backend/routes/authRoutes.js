const express = require('express');
const router = express.Router();
const { loginUser, getAllUser } = require('../controllers/authController');

router.post('/login', loginUser);
router.get('/getAllUsers', getAllUser)

module.exports = router;
