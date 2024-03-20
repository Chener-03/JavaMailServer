CREATE TABLE `email_info` (
                              `id` int(11) NOT NULL AUTO_INCREMENT,
                              `uuid` varchar(255) DEFAULT NULL,
                              `from` varchar(255) DEFAULT NULL,
                              `to` varchar(255) DEFAULT NULL,
                              `content` longtext,
                              `subject` varchar(255) DEFAULT NULL,
                              `username` varchar(255) DEFAULT NULL,
                              `create_time` datetime DEFAULT NULL,
                              `read_time` datetime DEFAULT NULL,
                              `type` int(11) DEFAULT NULL,
                              `state` int(11) DEFAULT NULL,
                              `sendFailMsg` varchar(1024) DEFAULT NULL,
                              `isdel` int(11) DEFAULT NULL,
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_info` (
                             `id` int(11) NOT NULL AUTO_INCREMENT,
                             `username` varchar(255) DEFAULT NULL,
                             `password` varchar(255) DEFAULT NULL,
                             `phone` varchar(255) DEFAULT NULL,
                             `create_time` datetime DEFAULT NULL,
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `un` (`username`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4;