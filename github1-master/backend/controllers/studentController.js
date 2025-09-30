const { PrismaClient } = require('@prisma/client');
const bcrypt = require('bcrypt');
const prisma = new PrismaClient();

exports.createStudent = async (req, res) => {
  const {
    name, register_number, course, year, email,
    phone, gender, dob, photo_url
  } = req.body;

  try {
    const existingUser = await prisma.user.findFirst({ where: { email } });
    if (existingUser) return res.status(409).json({ error: 'User already exists' });

    const roleRecord = await prisma.role.findFirst({ where: { name: 'student' } });
    if (!roleRecord) return res.status(400).json({ error: 'Student role not found' });

    const hashedPassword = await bcrypt.hash('student@123', 10);

    const user = await prisma.user.create({
      data: {
        username: register_number,
        email,
        password: hashedPassword,
        role_id: roleRecord.id
      }
    });

  const data = {
  user_id: user.id,
};

if (name) data.name = name;
if (register_number) data.register_number = register_number;
if (course) data.course = course;
if (year) data.year = parseInt(year);
if (email) data.email = email;
if (phone) data.phone = phone;
if (gender) data.gender = gender;
if (dob) data.dob = new Date(dob);
if (photo_url) data.photo_url = photo_url;

const student = await prisma.student.create({ data });

 const students = await prisma.student.findMany({
    });

    return res.status(201).json({status:201,data:students});
  } catch (error) {
    console.error('Create Student Error:', error);
    res.status(500).json({ error: 'Failed to create student' });
  }
};

// controllers/studentController.js

exports.getAllStudents = async (req, res) => {
  try {
    const students = await prisma.student.findMany({
      include: {
        
      },
    });

    return res.status(200).json(students);
  } catch (error) {
    console.error('Error fetching students:', error);
    res.status(500).json({ error: 'Failed to fetch students' });
  }
};

exports.updateStudent = async (req, res) => {
  const { id } = req.params;
  const {
    name, register_number, course, year, email,
    phone, gender, dob, photo_url
  } = req.body;

  try {
      const updateData = {
    name,
    register_number,
    course,
    year: parseInt(year),
    email,
    phone,
    gender,
    photo_url
  };

  // Only add dob if it exists
  if (dob) {
    updateData.dob = new Date(dob);
  }

   const updated = await prisma.student.update({
    where: { user_id: id },
    data: updateData
  });


    return res.status(200).json({status:200,data:updated});
  } catch (error) {
    console.error('Update Student Error:', error);
    res.status(500).json({ error: 'Failed to update student' });
  }
};

exports.deleteStudent = async (req, res) => {
  const { id } = req.params;

  try {
    const student = await prisma.student.findUnique({ where: { id} });
    if (!student) return res.status(404).json({ error: 'Student not found' });

    await prisma.student.delete({ where: { id } });
    await prisma.user.delete({ where: { id: student.user_id } });

    return res.status(200).json({status:200, message: 'Student and linked user deleted' });
  } catch (error) {
    console.error('Delete Student Error:', error);
    res.status(500).json({ error: 'Failed to delete student' });
  }
};


exports.assignCourseToStudent = async (req, res) => {
  const { studentId, courseId } = req.body;

  try {
    const existing = await prisma.studentCourse.findUnique({
      where: {
        student_id_course_id: {
          student_id: studentId,
          course_id: courseId,
        }
      }
    });

    if (existing) {
      return res.status(400).json({ error: 'Course already assigned to student' });
    }

    const newAssignment = await prisma.studentCourse.create({
      data: {
        student_id: studentId,
        course_id: courseId
      }
    });

    res.status(201).json(newAssignment);
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: 'Failed to assign course' });
  }
};

exports.getCoursesForStudent = async (req, res) => {
  const { studentId } = req.params;

  try {
    const courses = await prisma.studentCourse.findMany({
      where: { student_id: studentId },
      include: {
        course: true
      }
    });

    res.json(courses);
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: 'Failed to fetch courses' });
  }
};

exports.updateStudentCourse = async (req, res) => {
  const { studentId, courseId } = req.params;
  const { newCourseId } = req.body;

  try {
    // Delete old mapping
    await prisma.studentCourse.delete({
      where: {
        student_id_course_id: {
          student_id: studentId,
          course_id: courseId
        }
      }
    });

    // Add new mapping
    const updated = await prisma.studentCourse.create({
      data: {
        student_id: studentId,
        course_id: newCourseId
      }
    });

    res.json(updated);
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: 'Failed to update student course' });
  }
};

exports.removeCourseFromStudent = async (req, res) => {
  const { studentId, courseId } = req.params;

  try {
    await prisma.studentCourse.delete({
      where: {
        student_id_course_id: {
          student_id: studentId,
          course_id: courseId
        }
      }
    });

    res.json({ message: 'Course removed from student' });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: 'Failed to remove course' });
  }
};



exports.viewAnnouncements = async (req, res) => {
  const { studentId } = req.params;

  try {
    // Step 1: Get all student_course records for this student
    const studentCourses = await prisma.studentCourse.findMany({
      where: { student_id: studentId },
      select: { id: true } // Only need IDs
    });

    const courseIds = studentCourses.map(sc => sc.id); // Array of studentCourse IDs

    // Step 2: Get announcements linked to those student_course entries
    const announcements = await prisma.studentCourseAnnouncement.findMany({
      where: {
        student_course_id: { in: courseIds }
      },
      include: {
        announcement: true
      }
    });

    // Step 3: Extract just the announcement data
    const result = announcements.map(entry => entry.announcement);

    res.status(200).json(result);
  } catch (error) {
    console.error('View Announcements Error:', error);
    res.status(500).json({ error: 'Failed to fetch student announcements' });
  }
};



    
exports.getOneStudents = async(req,res) =>{
  try{
    const {id} = req.params;
    const getStudentData = await prisma.student.findFirst({
      where:{
        user_id:id
      }
    })
    const getCourseData = await prisma.studentCourse.findMany({
      where:{
        student_id:getStudentData.id
      }
    })
    let data = {
      studentData:getStudentData,
      studentCourse:getCourseData
    }
    return res.status(200).json(data)
  }catch(error){
    console.log(error)
     res.status(500).json({ error: "Unable to fetch announcements" });
  }
}