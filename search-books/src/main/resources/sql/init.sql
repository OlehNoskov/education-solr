DROP TABLE IF EXISTS books CASCADE;
DROP TABLE IF EXISTS book_tags CASCADE;
DROP TABLE IF EXISTS tags;

CREATE TABLE books
(
    id               SERIAL PRIMARY KEY,
    author           VARCHAR(100) NOT NULL,
    title            VARCHAR(100) NOT NULL,
    description      TEXT,
    publication_date TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE tags
(
    id               SERIAL PRIMARY KEY,
    tag              VARCHAR(100) NOT NULL
);

CREATE TABLE book_tags
(
    id               SERIAL PRIMARY KEY,
    book_id          INTEGER REFERENCES books(id) ON DELETE CASCADE,
    tag_id           INTEGER REFERENCES tags(id) ON DELETE CASCADE
);

ALTER SEQUENCE books_id_seq RESTART WITH 11;
ALTER SEQUENCE tags_id_seq RESTART WITH 13;
ALTER SEQUENCE book_tags_id_seq RESTART WITH 20;

INSERT INTO postgres.public.books (id, author, title, description, publication_date)
VALUES (1, 'Robert Martin', 'Clean Code: A Handbook of Agile Software Craftsmanship',
        'Clean Code is divided into three parts. The first describes the principles, ' ||
        'patterns, and practices of writing clean code. The second part consists of several ' ||
        'case studies of increasing complexity. Each case study is an exercise in cleaning up c' ||
        'ode—of transforming a code base that has some problems into one that is sound and efficient. ' ||
        'The third part is the payoff: a single chapter containing a list of heuristics and “smells” ' ||
        'gathered while creating the case studies. The result is a knowledge base that describes the way ' ||
        'we think when we write, read, and clean code.','2019-11-01T23:00:00Z');

INSERT INTO postgres.public.books (id, author, title, description, publication_date)
VALUES (2, 'Bruce Eckel', 'Thinking in Java',
        'Thinking in Java has earned raves from programmers worldwide for its extraordinary clarity,' ||
        'careful organization, and small, direct programming examples. ' ||
        'From the fundamentals of Java syntax to its most advanced features, ' ||
        'Thinking in Java is designed to teach, one simple step at a time.', '2019-12-01T23:59:59Z');

INSERT INTO postgres.public.books (id, author, title, description, publication_date)
VALUES (3, 'Erik Oberg', 'Clean Code: A Handbook of Agile Software Craftsmanship',
        'Clean Code is divided into three parts. The first describes the principles, ' ||
        'patterns, and practices of writing clean code. The second part consists of several ' ||
        'case studies of increasing complexity. Each case study is an exercise in cleaning up c' ||
        'ode—of transforming a code base that has some problems into one that is sound and efficient. ' ||
        'The third part is the payoff: a single chapter containing a list of heuristics and “smells” ' ||
        'gathered while creating the case studies. The result is a knowledge base that describes the way ' ||
        'we think when we write, read, and clean code.', '2015-12-01T23:59:59Z');

INSERT INTO postgres.public.books (id, author, title, description, publication_date)
VALUES (4, 'unknown_writer', 'Java for absolute beginners. The best manual for students, which are learning java',
        'A good guide for people new to Java programming language', '2014-12-01T23:59:59Z');

INSERT INTO postgres.public.books (id, author, title, description, publication_date)
VALUES (5, 'Bert Bates', 'Head First. Java',
        'By exploiting how your brain works, Head First Java compresses the time it takes to learn ' ||
        'and retain--complex information. Its unique approach not only shows you what you need to ' ||
        'know about Java syntax, it teaches you to think like a Java programmer. ' ||
        'If you want to be bored, buy some other book. But if you want to understand Java, this books for you.'
           , '2003-08-01T23:00:00Z');

INSERT INTO postgres.public.books (id, author, title, description, publication_date)
VALUES (6, 'unknown_writer', 'Holy bible',
        'The ESV Church Bible is an affordable, practical choice for churches to use as part of their ' ||
        'weekly worship services or for outreach. The low price combined with a durable cover make ' ||
        'this a great value for use in the pews, as gifts for church visitors, or in widespread ministry efforts.',
        '2016-07-01T23:59:59Z');

INSERT INTO postgres.public.books (id, author, title, description, publication_date)
VALUES (7, 'Stephen King', 'The Green mile',
        'At Cold Mountain Penitentiary, along the lonely stretch of cells known as the Green Mile, ' ||
        'Billy the Kid Wharton and the possessed Eduard Delacroix await death strapped in Old Sparky. ' ||
        'But good or evil, innocent or guilty, prisoner or guard, none has ever seen the brutal likes ' ||
        'of the new prisoner, John Coffey, sentenced to death for raping and murdering two young girls. ' ||
        'Is Coffey a devil in human form? Or is he a far, far different kind of being?' ||
        'condemned killers such as Billy the Kid Wharton and the possessed Eduard Delacroix ' ||
        'await death strapped in Old Sparky. But good or evil, innocent or guilty, prisoner or guard, ' ||
        'none has ever seen the brutal likes of the new prisoner, John Coffey, sentenced to death for' ||
        ' raping and murdering two young girls. Is Coffey a devil in human form? Or is he a far, ' ||
        'far different kind of being?', '2005-12-01T23:59:59Z');

INSERT INTO postgres.public.books (id, author, title, description, publication_date)
VALUES (8, 'Erich Maria Remarque', 'All Quiet on the Western Front',
        'This is the testament of Paul Bäumer, who enlists with his classmates in the ' ||
        'German army during World War I. They become soldiers with youthful enthusiasm. ' ||
        'But the world of duty, culture, and progress they had been taught breaks in pieces under ' ||
        'the first bombardment in the trenches.', '2014-12-01T23:59:59Z');

INSERT INTO postgres.public.books (id, author, title, description, publication_date)
VALUES (9, 'Irwin Shaw', 'The Young Lions',
        'The Young Lions is a vivid and classic novel that portrays the experiences of ordinary ' ||
        'soldiers fighting World War II. Told from the points of view of a perceptive young Nazi, ' ||
        'a jaded American film producer, and a shy Jewish boy just married to the love of his life, ' ||
        'Shaw conveys, as no other novelist has since, the scope, confusion, and complexity of war.',
        '2004-12-01T23:59:59Z');

INSERT INTO postgres.public.books (id, author, title, description, publication_date)
VALUES (10, 'Stephen King', 'Misery',
        'Bestselling novelist Paul Sheldon thinks he’s finally free of Misery Chastain. ' ||
        'In a controversial career move, he’s just killed off the popular protagonist of his beloved ' ||
        'romance series in favor of expanding his creative horizons. But such a change doesn’t ' ||
        'come without consequences. After a near-fatal car accident in rural Colorado leaves his ' ||
        'body broken, Paul finds himself at the mercy of the terrifying rescuer who’s nursing him back ' ||
        'to health—his self-proclaimed number one fan, Annie Wilkes. Annie is very upset over what ' ||
        'Paul did to Misery and demands that he find a way to bring her back by writing a new novel—his best yet,' ||
        ' and one that’s all for her. After all, Paul has all the time in the world to do so as a prisoner ' ||
        'in her isolated house...and Annie has some very persuasive and violent methods to get exactly what she wants...',
        '2012-12-01T23:59:59Z');

INSERT INTO postgres.public.tags  (id, tag) VALUES (1, 'Programming');
INSERT INTO postgres.public.tags  (id, tag) VALUES (2, 'Science');
INSERT INTO postgres.public.tags  (id, tag) VALUES (3, 'Sacred text');
INSERT INTO postgres.public.tags  (id, tag) VALUES (4, 'Parable');
INSERT INTO postgres.public.tags  (id, tag) VALUES (5, 'Modern edition');
INSERT INTO postgres.public.tags  (id, tag) VALUES (6, 'Historic');
INSERT INTO postgres.public.tags  (id, tag) VALUES (7, 'Drama');
INSERT INTO postgres.public.tags  (id, tag) VALUES (8, 'War movie');
INSERT INTO postgres.public.tags  (id, tag) VALUES (9, 'Horror');
INSERT INTO postgres.public.tags  (id, tag) VALUES (10, 'Detective');
INSERT INTO postgres.public.tags  (id, tag) VALUES (11, 'Engineering');
INSERT INTO postgres.public.tags  (id, tag) VALUES (12, 'Java');

INSERT INTO postgres.public.book_tags  (id, book_id, tag_id) VALUES (1,1,1);
INSERT INTO postgres.public.book_tags  (id, book_id, tag_id) VALUES (2,2,1);
INSERT INTO postgres.public.book_tags  (id, book_id, tag_id) VALUES (3,3,2);
INSERT INTO postgres.public.book_tags  (id, book_id, tag_id) VALUES (4,3,11);
INSERT INTO postgres.public.book_tags  (id, book_id, tag_id) VALUES (5,4,1);
INSERT INTO postgres.public.book_tags  (id, book_id, tag_id) VALUES (6,4,12);
INSERT INTO postgres.public.book_tags  (id, book_id, tag_id) VALUES (7,5,1);
INSERT INTO postgres.public.book_tags  (id, book_id, tag_id) VALUES (8,6,3);
INSERT INTO postgres.public.book_tags  (id, book_id, tag_id) VALUES (9,6,4);
INSERT INTO postgres.public.book_tags  (id, book_id, tag_id) VALUES (10,6,5);
INSERT INTO postgres.public.book_tags  (id, book_id, tag_id) VALUES (11,7,9);
INSERT INTO postgres.public.book_tags  (id, book_id, tag_id) VALUES (12,7,7);
INSERT INTO postgres.public.book_tags  (id, book_id, tag_id) VALUES (13,8,6);
INSERT INTO postgres.public.book_tags  (id, book_id, tag_id) VALUES (14,8,7);
INSERT INTO postgres.public.book_tags  (id, book_id, tag_id) VALUES (15,8,8);
INSERT INTO postgres.public.book_tags  (id, book_id, tag_id) VALUES (16,9,7);
INSERT INTO postgres.public.book_tags  (id, book_id, tag_id) VALUES (17,9,8);
INSERT INTO postgres.public.book_tags  (id, book_id, tag_id) VALUES (18,10,9);
INSERT INTO postgres.public.book_tags  (id, book_id, tag_id) VALUES (19,10,10);
