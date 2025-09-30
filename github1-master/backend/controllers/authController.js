const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

const JWT_SECRET = process.env.JWT_SECRET || 'secretkey';

exports.loginUser = async (req, res) => {
  const { username, password, role } = req.body;

  if (!username || !password || !role) {
    return res.status(400).json({ error: 'Missing fields' });
  }

  try {
    const user = await prisma.user.findFirst({ where: { username } ,include:{role:true}});
    console.log(user);
    if (!user || user.role.name !== role) {
      return res.status(401).json({ error: 'Invalid credentials or role' });
    }

    const validPassword = await bcrypt.compare(password, user.password);
    if (!validPassword) {
      return res.status(401).json({ error: 'Invalid credentials or role' });
    }

    const token = jwt.sign(
      { userId: user.id, username: user.username, role: user.role },
      JWT_SECRET,
      { expiresIn: '1h' }
    );

    res.json({ message: 'Login successful', token });
  } catch (error) {
    console.error(error);
    res.status(100).json({ error: 'Server error' });
  }
};


exports.getAllUser = async(req,res)=>{
  try{
  
const allUsers = await prisma.user.findMany()

    return res.json({status:200,data:allUsers})

  }catch(error){
    console.log(error)
  }
}