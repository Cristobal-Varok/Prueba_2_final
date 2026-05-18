ALTER TABLE users
ADD address VARCHAR(255);

UPDATE users
SET address = 'Sin dirección';

ALTER TABLE users
MODIFY address VARCHAR(255) NOT NULL;