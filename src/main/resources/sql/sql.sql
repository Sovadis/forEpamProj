CREATE
ALIAS IF NOT EXISTS gen_salt FOR "com.epam.rd.autocode.spring.project.security.H2Crypto.genSalt";
CREATE
ALIAS IF NOT EXISTS crypt FOR "com.epam.rd.autocode.spring.project.security.H2Crypto.crypt";

INSERT INTO EMPLOYEES (BIRTH_DATE, EMAIL, NAME, PASSWORD, PHONE, ROLE, BLOCKED)
VALUES ('1990-05-15', 'john.doe@email.com', 'John Doe', crypt('pass123', gen_salt('bf')), '555-123-4567', 'ROLE_EMPLOYEE', FALSE),
       ('1985-09-20', 'jane.smith@email.com', 'Jane Smith', crypt('abc456', gen_salt('bf')), '555-987-6543', 'ROLE_EMPLOYEE', FALSE),
       ('1978-03-08', 'bob.jones@email.com', 'Bob Jones', crypt('qwerty789', gen_salt('bf')), '555-321-6789', 'ROLE_EMPLOYEE', FALSE),
       ('1982-11-25', 'alice.white@email.com', 'Alice White', crypt('secret567', gen_salt('bf')), '555-876-5432', 'ROLE_EMPLOYEE', FALSE),
       ('1995-07-12', 'mike.wilson@email.com', 'Mike Wilson', crypt('mypassword', gen_salt('bf')), '555-234-5678', 'ROLE_EMPLOYEE', FALSE),
       ('1989-01-30', 'sara.brown@email.com', 'Sara Brown', crypt('letmein123', gen_salt('bf')), '555-876-5433', 'ROLE_EMPLOYEE', FALSE),
       ('1975-06-18', 'tom.jenkins@email.com', 'Tom Jenkins', crypt('pass4321', gen_salt('bf')), '555-345-6789', 'ROLE_EMPLOYEE', FALSE),
       ('1987-12-04', 'lisa.taylor@email.com', 'Lisa Taylor', crypt('securepwd', gen_salt('bf')), '555-789-0123', 'ROLE_EMPLOYEE', FALSE),
       ('1992-08-22', 'david.wright@email.com', 'David Wright', crypt('access123', gen_salt('bf')), '555-456-7890', 'ROLE_EMPLOYEE', FALSE),
       ('1980-04-10', 'emily.harris@email.com', 'Emily Harris', crypt('1234abcd', gen_salt('bf')), '555-098-7654', 'ROLE_EMPLOYEE', FALSE);


INSERT INTO CLIENTS (BALANCE, EMAIL, NAME, PASSWORD, ROLE, BLOCKED)
VALUES (1000.00, 'client1@example.com', 'Medelyn Wright', crypt('password123', gen_salt('bf')), 'ROLE_CLIENT', FALSE),
       (1500.50, 'client2@example.com', 'Landon Phillips', crypt('securepass', gen_salt('bf')), 'ROLE_CLIENT', FALSE),
       (800.75, 'client3@example.com', 'Harmony Mason', crypt('abc123', gen_salt('bf')), 'ROLE_CLIENT', FALSE),
       (1200.25, 'client4@example.com', 'Archer Harper', crypt('pass456', gen_salt('bf')), 'ROLE_CLIENT', FALSE),
       (900.80, 'client5@example.com', 'Kira Jacobs', crypt('letmein789', gen_salt('bf')), 'ROLE_CLIENT', FALSE),
       (1100.60, 'client6@example.com', 'Maximus Kelly', crypt('adminpass', gen_salt('bf')), 'ROLE_CLIENT', FALSE),
       (1300.45, 'client7@example.com', 'Sierra Mitchell', crypt('mypassword', gen_salt('bf')), 'ROLE_CLIENT', FALSE),
       (950.30, 'client8@example.com', 'Quinton Saunders', crypt('test123', gen_salt('bf')), 'ROLE_CLIENT', FALSE),
       (1050.90, 'client9@example.com', 'Amina Clarke', crypt('qwerty123', gen_salt('bf')), 'ROLE_CLIENT', FALSE),
       (880.20, 'client10@example.com', 'Bryson Chavez', crypt('pass789', gen_salt('bf')), 'ROLE_CLIENT', FALSE);

INSERT INTO BOOKS (name, genre, age_group, price, publication_year, author, number_of_pages, characteristics,
                   description, language)
VALUES ('The Hidden Treasure', 'Adventure', 'ADULT', 24.99, '2018-05-15', 'Emily White', 400, 'Mysterious journey',
        'An enthralling adventure of discovery', 'ENGLISH'),
       ('Echoes of Eternity', 'Fantasy', 'TEEN', 16.50, '2011-01-15', 'Daniel Black', 350, 'Magical realms',
        'A spellbinding tale of magic and destiny', 'ENGLISH'),
       ('Whispers in the Shadows', 'Mystery', 'ADULT', 29.95, '2018-08-11', 'Sophia Green', 450, 'Intriguing suspense',
        'A gripping mystery that keeps you guessing', 'ENGLISH'),
       ('The Starlight Sonata', 'Romance', 'ADULT', 21.75, '2011-05-15', 'Michael Rose', 320, 'Heartwarming love story',
        'A beautiful journey of love and passion', 'ENGLISH'),
       ('Beyond the Horizon', 'Science Fiction', 'CHILD', 18.99, '2004-05-15', 'Alex Carter', 280,
        'Interstellar adventure', 'An epic sci-fi adventure beyond the stars', 'ENGLISH'),
       ('Dancing with Shadows', 'Thriller', 'ADULT', 26.50, '2015-05-15', 'Olivia Smith', 380, 'Suspenseful twists',
        'A thrilling tale of danger and intrigue', 'ENGLISH'),
       ('Voices in the Wind', 'Historical Fiction', 'ADULT', 32.00, '2017-05-15', 'William Turner', 500,
        'Rich historical setting', 'A compelling journey through time', 'ENGLISH'),
       ('Serenade of Souls', 'Fantasy', 'TEEN', 15.99, '2013-05-15', 'Isabella Reed', 330, 'Enchanting realms',
        'A magical fantasy filled with wonder', 'ENGLISH'),
       ('Silent Whispers', 'Mystery', 'ADULT', 27.50, '2021-05-15', 'Benjamin Hall', 420, 'Intricate detective work',
        'A mystery that keeps you on the edge', 'ENGLISH'),
       ('Whirlwind Romance', 'Romance', 'OTHER', 23.25, '2022-05-15', 'Emma Turner', 360, 'Passionate love affair',
        'A romance that sweeps you off your feet', 'ENGLISH');
