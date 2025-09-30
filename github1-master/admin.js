
document.addEventListener('DOMContentLoaded', function () {
  const token = localStorage.getItem('authToken');
  const errorMsg = document.getElementById('login-error'); // Optional: for displaying errors

  axios.get('http://localhost:4000/api/student/', {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  })
  .then(response => { 
 renderStudents(response.data);
     
  })
  .catch(error => {
    console.error('Error fetching student data:', error);
    if (errorMsg) {
      errorMsg.style.color = "#a52a2a";
      errorMsg.textContent = "Failed to fetch student profile.";
    }
  });
});



document.getElementById('studentEditForm').addEventListener('submit', function(e) {
  e.preventDefault();

// Get the last row (User ID is in the last <tr>)
const userId = document.getElementById("editUserId").value 
console.log('User ID:', userId);
  const updatedData = {
    register_number: document.getElementById('editStudentRoll').value,
    name: document.getElementById('editStudentName').value,
    email: document.getElementById('editStudentEmail').value,
    course: document.getElementById('editStudentClass').value,
    phone: document.getElementById('editStudentPhone').value,
    gender: document.getElementById('editStudentGender').value,
    dob: document.getElementById('editStudentDob').value,
    year: parseInt(document.getElementById('editStudentYear').value),
  };

  const token = localStorage.getItem('authToken');
  axios.put(`http://localhost:4000/api/student/${userId}`, updatedData, {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  }).then((response) => {
    console.log(response.data,"gjdfhfskjh")
    if(response.data.status === 200){
            alert("updated student successFully.");
         closeStudentEdit();
         console.log(response.data.data)
    fetchStudents(); // re-fetch and render
    }else{
    alert("Failed to update student.");
    }
   
  }).catch(err => {
    console.error('Update failed', err);
  });
});


document.getElementById('studentEditForm').addEventListener('submit', function(e) {
  e.preventDefault();

// Get the last row (User ID is in the last <tr>)
const userId = document.getElementById("editUserId").value 
console.log('User ID:', userId);

  axios.delete(`http://localhost:4000/api/student/${userId}`, {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  }).then((response) => {
    if(response.data.status === 200){
        alert("Deleted student successFully.");
        renderStudents();// re-fetch and render
    }else{
    alert("Failed to Delete student.");
    }
   
  }).catch(err => {
    console.error('Update failed', err);
  });
});


document.getElementById('studentForm').addEventListener('submit', function (e) {
  e.preventDefault();

  const data = {
    register_number: document.getElementById('studentRoll').value,
    name: document.getElementById('studentName').value,
    email: document.getElementById('studentEmail').value,
    year: parseInt(document.getElementById('studentClass').value),
    phone: document.getElementById("studentPhone").value,
    dob: document.getElementById("studentDOB").value,
    gender: document.getElementById("studentGender").value,
  };

  const errorMsg = document.getElementById('login-error');

  axios.post('http://localhost:4000/api/student/', data, {
    headers: { 'Content-Type': 'application/json' }
  })
  .then(res => {
    console.log("Student added:", res.data);
  
    alert("Student added successfully.");
    // Optionally clear form
    document.getElementById('studentForm').reset();
    renderStudents(res.data.data); // Refresh table
  })
  .catch(err => {
    console.error(err);
    errorMsg.style.color = "#a52a2a";
    errorMsg.textContent = "Failed to add student.";
  });
});
