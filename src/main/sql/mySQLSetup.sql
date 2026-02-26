CREATE DATABASE OOP_gca2;
USE DATABASE OOP_gca2;

-- user account, if not confirmed / confirm if it works
CREATE USER 'OOP_gca2_user'@'localhost' IDENTIFIED VIA mysql_native_password USING '***';GRANT ALL PRIVILEGES ON *.* TO 'OOP_gca2_user'@'localhost' REQUIRE NONE WITH GRANT OPTION MAX_QUERIES_PER_HOUR 0 MAX_CONNECTIONS_PER_HOUR 0 MAX_UPDATES_PER_HOUR 0 MAX_USER_CONNECTIONS 0;GRANT ALL PRIVILEGES ON `OOP\_gca2`.* TO 'OOP_gca2_user'@'localhost';

CREATE TABLE location (
     id INT AUTO_INCREMENT PRIMARY KEY,
     latitude DECIMAL(10, 8) NOT NULL,
     longitude DECIMAL(11, 8) NOT NULL,
     full_address VARCHAR(255),
     created_at DATETIME NOT NULL
 );

 CREATE TABLE route_stop (
     id INT AUTO_INCREMENT PRIMARY KEY,
     route_name VARCHAR(100) NOT NULL,
     location_id INT NOT NULL,
     created_at DATETIME NOT NULL,
     FOREIGN KEY (location_id) REFERENCES location(id)
 );

 CREATE TABLE trail (
     id INT AUTO_INCREMENT PRIMARY KEY,
     name VARCHAR(100) NOT NULL,
     description TEXT,
     difficulty ENUM('Easy', 'Moderate', 'Hard') NOT NULL,
     estimated_time DECIMAL(5,2)
 );

 CREATE TABLE trail_stop (
     trail_id INT NOT NULL,
     stop_id INT NOT NULL,
     PRIMARY KEY (trail_id, stop_id),
     FOREIGN KEY (trail_id) REFERENCES trail(id),
     FOREIGN KEY (stop_id) REFERENCES route_stop(id)
 );

 CREATE TABLE trail_media (
     id INT AUTO_INCREMENT PRIMARY KEY,
     trail_id INT NOT NULL,
     stop_id INT NULL,
     media_type ENUM('IMAGE', 'VIDEO', 'AUDIO') NOT NULL,
     url VARCHAR(255) NOT NULL,
     caption VARCHAR(255),
     creation_time DATETIME NOT NULL,
     FOREIGN KEY (trail_id) REFERENCES trail(id),
     FOREIGN KEY (stop_id) REFERENCES route_stop(id)
 );

INSERT INTO location (latitude, longitude, full_address, created_at) VALUES
(40.7128, -74.0060, 'New York, NY', '2023-01-15 10:00:00'),
(34.0522, -118.2437, 'Los Angeles, CA', '2023-02-20 11:30:00'),
(41.8781, -87.6298, 'Chicago, IL', '2023-03-10 09:15:00'),
(29.7604, -95.3698, 'Houston, TX', '2023-04-05 14:20:00'),
(33.4484, -112.0740, 'Phoenix, AZ', '2023-05-12 16:45:00'),
(39.9526, -75.1652, 'Philadelphia, PA', '2023-06-18 08:00:00'),
(29.4241, -98.4936, 'San Antonio, TX', '2023-07-22 13:10:00'),
(32.7157, -117.1611, 'San Diego, CA', '2023-08-30 10:30:00'),
(32.7767, -96.7970, 'Dallas, TX', '2023-09-14 12:00:00'),
(37.7749, -122.4194, 'San Francisco, CA', '2023-10-01 15:00:00');

INSERT INTO route_stop (route_name, location_id, created_at) VALUES
('Start Point', 1, '2023-01-16 09:00:00'),
('Downtown', 2, '2023-02-21 10:30:00'),
('Lake View', 3, '2023-03-11 08:45:00'),
('Museum District', 4, '2023-04-06 13:15:00'),
('Desert Lookout', 5, '2023-05-13 11:20:00'),
('Historic District', 6, '2023-06-19 07:30:00'),
('River Walk', 7, '2023-07-23 14:00:00'),
('Beach Front', 8, '2023-08-31 09:45:00'),
('Arts District', 9, '2023-09-15 10:15:00'),
('Golden Gate View', 10, '2023-10-02 16:30:00');

INSERT INTO trail (name, description, difficulty, estimated_time) VALUES
('City Explorer', 'A walk through downtown highlights', 'Easy', 2.5),
('Mountain Ridge', 'Challenging hike with scenic overlooks', 'Hard', 5.0),
('Lake Loop', 'Gentle path around the lake', 'Easy', 1.5),
('Desert Trail', 'Hot and dry, best in early morning', 'Moderate', 3.0),
('Historic Route', 'Passes by landmarks and museums', 'Moderate', 4.0),
('Coastal Walk', 'Breezy path along the shore', 'Easy', 2.0),
('Canyon Descent', 'Steep drop with amazing views', 'Hard', 4.5),
('Park Stroll', 'Leisurely walk through the park', 'Easy', 1.0),
('Urban Hike', 'Mix of city streets and green spaces', 'Moderate', 3.5),
('Sunset Trail', 'Perfect for evening walks', 'Easy', 2.0);

INSERT INTO trail_stop (trail_id, stop_id) VALUES
(1, 1),
(1, 2),
(1, 3),
(2, 4),
(2, 5),
(3, 6),
(3, 7),
(4, 8),
(4, 9),
(5, 10);

INSERT INTO trail_media (trail_id, stop_id, media_type, url, caption, creation_time) VALUES
(1, 1, 'IMAGE', 'http://example.com/img/city_start.jpg', 'Starting point of City Explorer', '2023-01-17 10:00:00'),
(1, 2, 'IMAGE', 'http://example.com/img/downtown.jpg', 'Downtown skyline', '2023-01-18 11:30:00'),
(2, NULL, 'VIDEO', 'http://example.com/vid/mountain_overview.mp4', 'Aerial view of the ridge', '2023-02-22 14:15:00'),
(2, 4, 'IMAGE', 'http://example.com/img/steep_climb.jpg', 'Steep section of the trail', '2023-02-23 09:45:00'),
(3, 6, 'IMAGE', 'http://example.com/img/lake_morning.jpg', 'Lake at sunrise', '2023-03-12 07:20:00'),
(3, 7, 'AUDIO', 'http://example.com/audio/birds.ogg', 'Birds chirping near the lake', '2023-03-13 08:10:00'),
(4, 8, 'IMAGE', 'http://example.com/img/desert_panorama.jpg', 'Vast desert landscape', '2023-05-14 06:30:00'),
(5, 10, 'VIDEO', 'http://example.com/vid/historic_walk.mp4', 'Walking tour of historic district', '2023-09-16 13:00:00'),
(6, NULL, 'IMAGE', 'http://example.com/img/coast_sunset.jpg', 'Sunset over the ocean', '2023-06-20 19:45:00'),
(7, 5, 'IMAGE', 'http://example.com/img/canyon_view.jpg', 'Looking down into the canyon', '2023-04-07 12:30:00');