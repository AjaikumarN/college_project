/*
  Warnings:

  - A unique constraint covering the columns `[facultyProfileId]` on the table `User` will be added. If there are existing duplicate values, this will fail.

*/
-- AlterTable
ALTER TABLE "User" ADD COLUMN     "facultyProfileId" INTEGER;

-- CreateTable
CREATE TABLE "FacultyProfile" (
    "id" SERIAL NOT NULL,
    "facultyId" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "department" TEXT NOT NULL,
    "gender" TEXT NOT NULL,
    "dob" TEXT NOT NULL,
    "email" TEXT NOT NULL,
    "phone" TEXT NOT NULL,
    "qualification" TEXT NOT NULL,
    "experience" TEXT NOT NULL,
    "photo" TEXT NOT NULL,

    CONSTRAINT "FacultyProfile_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "FacultyProfile_facultyId_key" ON "FacultyProfile"("facultyId");

-- CreateIndex
CREATE UNIQUE INDEX "User_facultyProfileId_key" ON "User"("facultyProfileId");

-- AddForeignKey
ALTER TABLE "User" ADD CONSTRAINT "User_facultyProfileId_fkey" FOREIGN KEY ("facultyProfileId") REFERENCES "FacultyProfile"("id") ON DELETE SET NULL ON UPDATE CASCADE;
