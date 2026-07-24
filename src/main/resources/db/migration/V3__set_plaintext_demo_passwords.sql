-- Re-apply BCrypt hashes for seeded users (password plaintext = email).
-- Safe to re-run intent for DBs that still have plaintext or older hashes.

UPDATE users SET password = '$2a$10$QKSzh34IUMQS1W63OwZR.uU/UOOe1siUzg5zZQGdVH/HOfeChmD.G', updated_at = CURRENT_TIMESTAMP WHERE id = 1;
UPDATE users SET password = '$2a$10$QnrfirMw9Wi.R8aTw2670Oo4X0ADXM07.QwB5v4u0olNmjT..z5QW', updated_at = CURRENT_TIMESTAMP WHERE id = 2;
UPDATE users SET password = '$2a$10$sI1uWbYK9smYzcXe7RA3XeBshYLD/8iEh6IsonrYZRRUuuuT74E/W', updated_at = CURRENT_TIMESTAMP WHERE id = 3;
UPDATE users SET password = '$2a$10$aXhJJDooKZ5jfApNv1OSXuuAilyWgyJ.sEN0o7ZX0hlt7/lxfgF2i', updated_at = CURRENT_TIMESTAMP WHERE id = 4;
UPDATE users SET password = '$2a$10$Ar3DHGel7RMpFrdkg7Y7C.G2B9UhlW6V8k0THB7PNQT.3/zBoRH7i', updated_at = CURRENT_TIMESTAMP WHERE id = 5;
