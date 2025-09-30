-- AlterTable
ALTER TABLE "departments" ADD COLUMN     "year" TEXT;

-- CreateTable
CREATE TABLE "faculty_education" (
    "id" TEXT NOT NULL,
    "facultyId" TEXT NOT NULL,
    "degree" TEXT NOT NULL,
    "specialization" TEXT NOT NULL,
    "institutionName" TEXT NOT NULL,
    "startYear" INTEGER NOT NULL,
    "endYear" INTEGER NOT NULL,
    "grade" TEXT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "faculty_education_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "faculty_experience" (
    "id" TEXT NOT NULL,
    "facultyId" TEXT NOT NULL,
    "institutionName" TEXT NOT NULL,
    "designation" TEXT NOT NULL,
    "startDate" TIMESTAMP(3) NOT NULL,
    "endDate" TIMESTAMP(3),
    "isCurrent" BOOLEAN NOT NULL,
    "onboardDate" TIMESTAMP(3),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "faculty_experience_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "faculty_academic_details" (
    "id" TEXT NOT NULL,
    "facultyId" TEXT NOT NULL,
    "subjectHandled" TEXT NOT NULL,
    "paperPublished" TEXT NOT NULL,
    "certifications" TEXT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "faculty_academic_details_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "StudentMarks" (
    "id" TEXT NOT NULL,
    "student_id" TEXT NOT NULL,
    "department_id" TEXT NOT NULL,
    "course_id" TEXT NOT NULL,
    "total_marks" INTEGER NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "StudentMarks_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "StudentMarks_student_id_course_id_key" ON "StudentMarks"("student_id", "course_id");

-- AddForeignKey
ALTER TABLE "faculty_education" ADD CONSTRAINT "faculty_education_facultyId_fkey" FOREIGN KEY ("facultyId") REFERENCES "faculty"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "faculty_experience" ADD CONSTRAINT "faculty_experience_facultyId_fkey" FOREIGN KEY ("facultyId") REFERENCES "faculty"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "faculty_academic_details" ADD CONSTRAINT "faculty_academic_details_facultyId_fkey" FOREIGN KEY ("facultyId") REFERENCES "faculty"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "StudentMarks" ADD CONSTRAINT "StudentMarks_student_id_fkey" FOREIGN KEY ("student_id") REFERENCES "student"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "StudentMarks" ADD CONSTRAINT "StudentMarks_course_id_fkey" FOREIGN KEY ("course_id") REFERENCES "courses"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "StudentMarks" ADD CONSTRAINT "StudentMarks_department_id_fkey" FOREIGN KEY ("department_id") REFERENCES "departments"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
