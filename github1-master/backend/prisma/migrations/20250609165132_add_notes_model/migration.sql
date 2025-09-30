-- CreateTable
CREATE TABLE "Note" (
    "id" SERIAL NOT NULL,
    "course" TEXT NOT NULL,
    "fileUrl" TEXT NOT NULL,

    CONSTRAINT "Note_pkey" PRIMARY KEY ("id")
);
