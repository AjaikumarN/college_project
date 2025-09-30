/*
  Warnings:

  - You are about to drop the column `facultyProfileId` on the `User` table. All the data in the column will be lost.
  - You are about to drop the `FacultyProfile` table. If the table is not empty, all the data it contains will be lost.
  - A unique constraint covering the columns `[facultyId]` on the table `User` will be added. If there are existing duplicate values, this will fail.

*/
-- DropForeignKey
ALTER TABLE "User" DROP CONSTRAINT "User_facultyProfileId_fkey";

-- DropIndex
DROP INDEX "User_facultyProfileId_key";

-- AlterTable
ALTER TABLE "User" DROP COLUMN "facultyProfileId",
ADD COLUMN     "facultyId" INTEGER;

-- DropTable
DROP TABLE "FacultyProfile";

-- CreateTable
CREATE TABLE "Faculty" (
    "id" SERIAL NOT NULL,
    "facultyId" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "department" TEXT NOT NULL,
    "gender" TEXT NOT NULL,
    "dob" TIMESTAMP(3) NOT NULL,
    "email" TEXT NOT NULL,
    "phone" TEXT NOT NULL,
    "qualification" TEXT NOT NULL,
    "experience" TEXT NOT NULL,
    "photoUrl" TEXT NOT NULL,

    CONSTRAINT "Faculty_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "Faculty_facultyId_key" ON "Faculty"("facultyId");

-- CreateIndex
CREATE UNIQUE INDEX "User_facultyId_key" ON "User"("facultyId");

-- AddForeignKey
ALTER TABLE "User" ADD CONSTRAINT "User_facultyId_fkey" FOREIGN KEY ("facultyId") REFERENCES "Faculty"("id") ON DELETE SET NULL ON UPDATE CASCADE;
