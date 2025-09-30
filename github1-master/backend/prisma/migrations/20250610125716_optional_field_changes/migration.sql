-- DropForeignKey
ALTER TABLE "faculty" DROP CONSTRAINT "faculty_department_id_fkey";

-- AlterTable
ALTER TABLE "faculty" ALTER COLUMN "department_id" DROP NOT NULL;

-- AddForeignKey
ALTER TABLE "faculty" ADD CONSTRAINT "faculty_department_id_fkey" FOREIGN KEY ("department_id") REFERENCES "departments"("id") ON DELETE SET NULL ON UPDATE CASCADE;
