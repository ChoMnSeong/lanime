INSERT INTO admin (email, password)
VALUES ('jmsun0305@gmail.com', '$2a$10$9Bc9E8902LACo5/Ld1.mceJyf1uFrAiHpycFAyf2U3iBjnlwW0SQW')
ON CONFLICT (email) DO NOTHING;
