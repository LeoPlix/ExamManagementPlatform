-- Initial seed script for the EMS database
-- Contains: 2 Schools, 4 Disciplines, 1 Administrator, 2 School Staff, 3 Teachers, 10 Students

-- Schools
INSERT INTO schools (name, code, region) VALUES
('Escola Secundária de Camões', 'ESC-CAM', 'Lisboa e Vale do Tejo'),
('Escola Secundária D. Filipa de Lencastre', 'ESC-DFL', 'Lisboa e Vale do Tejo')
ON CONFLICT (code) DO NOTHING;

-- Disciplines
INSERT INTO disciplines (name, code) VALUES
('Matemática A', 'MAT-A'),
('Física e Química A', 'FQA'),
('Português', 'PORT'),
('Biologia e Geologia', 'BG')
ON CONFLICT (code) DO NOTHING;

-- Users (BCrypt encrypted passwords for default test credentials)
-- Passwords: 'admin123' / 'staff123' / 'teacher123' / 'student123'
-- Note: Spring Boot DataInitializer also automatically seeds these records on startup.

-- Administrator
INSERT INTO people (name, email, password, type, school_id) VALUES
('Administrador Geral', 'admin@dei.tecnico.ulisboa.pt', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ADMINISTRATOR', NULL)
ON CONFLICT (email) DO NOTHING;

-- School Staff
INSERT INTO people (name, email, password, type, school_id) VALUES
('Funcionário Camões', 'staff.camoes@dei.tecnico.ulisboa.pt', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'SCHOOL_STAFF', 1),
('Funcionário Filipa', 'staff.filipa@dei.tecnico.ulisboa.pt', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'SCHOOL_STAFF', 2)
ON CONFLICT (email) DO NOTHING;

-- Teachers
INSERT INTO people (name, email, password, type, school_id) VALUES
('Prof. António Matemática', 'prof.mat@dei.tecnico.ulisboa.pt', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'TEACHER', 1),
('Prof. Beatriz Física', 'prof.fqa@dei.tecnico.ulisboa.pt', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'TEACHER', 2),
('Prof. Carlos Português', 'prof.port@dei.tecnico.ulisboa.pt', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'TEACHER', 1)
ON CONFLICT (email) DO NOTHING;

-- Students (10 Students)
INSERT INTO people (name, email, password, type, school_id) VALUES
('Ana Silva', 'aluno1@dei.tecnico.ulisboa.pt', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'STUDENT', 1),
('Bernardo Santos', 'aluno2@dei.tecnico.ulisboa.pt', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'STUDENT', 1),
('Catarina Martins', 'aluno3@dei.tecnico.ulisboa.pt', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'STUDENT', 1),
('Diogo Ferreira', 'aluno4@dei.tecnico.ulisboa.pt', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'STUDENT', 1),
('Eduardo Costa', 'aluno5@dei.tecnico.ulisboa.pt', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'STUDENT', 1),
('Francisca Oliveira', 'aluno6@dei.tecnico.ulisboa.pt', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'STUDENT', 2),
('Gonçalo Pereira', 'aluno7@dei.tecnico.ulisboa.pt', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'STUDENT', 2),
('Helena Rodrigues', 'aluno8@dei.tecnico.ulisboa.pt', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'STUDENT', 2),
('Inês Sousa', 'aluno9@dei.tecnico.ulisboa.pt', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'STUDENT', 2),
('João Carvalho', 'aluno10@dei.tecnico.ulisboa.pt', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'STUDENT', 2)
ON CONFLICT (email) DO NOTHING;

-- Teacher - Discipline associations
INSERT INTO teacher_disciplines (teacher_id, discipline_id)
SELECT p.id, d.id FROM people p, disciplines d WHERE p.email = 'prof.mat@dei.tecnico.ulisboa.pt' AND d.code = 'MAT-A'
ON CONFLICT DO NOTHING;

INSERT INTO teacher_disciplines (teacher_id, discipline_id)
SELECT p.id, d.id FROM people p, disciplines d WHERE p.email = 'prof.fqa@dei.tecnico.ulisboa.pt' AND d.code = 'FQA'
ON CONFLICT DO NOTHING;

INSERT INTO teacher_disciplines (teacher_id, discipline_id)
SELECT p.id, d.id FROM people p, disciplines d WHERE p.email = 'prof.port@dei.tecnico.ulisboa.pt' AND d.code = 'PORT'
ON CONFLICT DO NOTHING;
