/*
  Warnings:

  - You are about to drop the column `faculty_id` on the `courses` table. All the data in the column will be lost.

*/
-- DropForeignKey
ALTER TABLE "courses" DROP CONSTRAINT "courses_faculty_id_fkey";

-- AlterTable
ALTER TABLE "courses" DROP COLUMN "faculty_id";
