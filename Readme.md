
# 🎓 LMS Platform

A modern Learning Management System (LMS) built with scalability and collaboration in mind.
It enables instructors to manage courses, admins to handle categories, and supports blogging and community features.
## 📂 Features

- 👩‍🏫 Instructor

    Perform CRUD operations on courses (Create, Read, Update, Delete).

    Manage course content and track enrollments.

    Write blog posts to share knowledge with students.

- 👨‍💼 Admin

    Add, edit, and remove categories for better course organization.

    Write and manage blog posts.

    Oversee instructors and students.

- 👩‍🎓 User / Student

    Browse and enroll in courses.

    Read blog posts and contribute by writing comments.

    View course progress and updates.

- 📊 Course Statistics
    Number of active courses.

    Total enrolled students.

    Instructor-wise course performance.

    Engagement stats for blogs and comments.

## 🧪 Features in Progress

- Exam & Quiz System – ongoing development.

- Advanced Analytics Dashboard for instructors and admins.

- Certificates on course completion.

- Gamification (badges, leaderboards).

- More features coming soon… 🚀
## ⚙️ Tech Stack (Example)

- Frontend: Next.js + Tailwind CSS

- Backend: Spring Boot

- Database: PostgreSQL

- Auth: JWT

## 🛠️ Installation

Install lms-spring

```bash
    git clone https://github.com/arihantjain916/lms-spring
    cd lms-spring
```

## 🧩 Prerequisites

Make sure you have the following installed:

- Java 24

- Maven

- PSQL

## 📖 Usage

- Admin dashboard → manage categories, instructors, blogs.

- Instructor dashboard → create/manage courses, blogs, track stats.

- User → browse courses, read blogs, add comments, enroll.


## ✅ Roadmap

- [x] Instructor CRUD for Courses  
- [x] Admin Category Management  
- [x] Blog & Comment System  
- [x] Course Stats  
- [x] Exams & Quizzes 
- [ ] Certificates  (in progress)
- [ ] Gamification  
- [ ] Notifications & Messaging  

## 📜 License

This project is licensed under the MIT License.

## Run with Docker

To publish and run the project using Docker:

1. **Build the Project**

```bash
   mvn clean package -Dmaven.test.skip=true
```
3. **Build Docker Image**

```bash
  docker-compose build
```
4. **Start Docker Container**

```bash
   docker-compose up -d
```
