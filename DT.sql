-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: restaurant_pos_system
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `area`
--

DROP TABLE IF EXISTS `area`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `area` (
  `area_id` int NOT NULL AUTO_INCREMENT,
  `area_name` varchar(10) NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `created_by` int DEFAULT NULL,
  `is_deleted` tinyint DEFAULT NULL,
  `deleted_by` int DEFAULT NULL,
  `deleted_at` timestamp NULL DEFAULT NULL,
  `status` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`area_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `area`
--

LOCK TABLES `area` WRITE;
/*!40000 ALTER TABLE `area` DISABLE KEYS */;
INSERT INTO `area` VALUES (1,'Room',NULL,NULL,NULL,NULL,NULL,NULL),(2,'Indoor',NULL,NULL,NULL,NULL,NULL,NULL),(3,'Outdoor',NULL,NULL,NULL,NULL,NULL,NULL),(4,'Room','2026-04-09 06:57:04',15,0,NULL,NULL,'Active'),(5,'Indoor','2026-04-09 06:59:16',15,0,NULL,NULL,'Active'),(6,'Outdoor','2026-04-09 06:59:28',15,0,NULL,NULL,'Active');
/*!40000 ALTER TABLE `area` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `category_id` int NOT NULL AUTO_INCREMENT,
  `category_name` varchar(45) NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `created_by` int DEFAULT NULL,
  `is_deleted` tinyint NOT NULL,
  `deleted_at` timestamp NULL DEFAULT NULL,
  `deleted_by` int DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  `updated_by` int DEFAULT NULL,
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,'main_dish',NULL,NULL,0,NULL,NULL,NULL,NULL),(2,'soup',NULL,NULL,0,NULL,NULL,NULL,NULL),(3,'drinks',NULL,NULL,0,NULL,NULL,NULL,NULL),(4,'dessert',NULL,NULL,0,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer` (
  `customer_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  `phone` varchar(11) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer`
--

LOCK TABLES `customer` WRITE;
/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
/*!40000 ALTER TABLE `customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory`
--

DROP TABLE IF EXISTS `inventory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventory` (
  `inventory_id` int NOT NULL AUTO_INCREMENT,
  `menu_item_id` int NOT NULL,
  `stock_in` int DEFAULT NULL,
  `stock_out` int DEFAULT NULL,
  `current_stock` int NOT NULL,
  `update_at` timestamp NOT NULL,
  `update_by` int NOT NULL,
  PRIMARY KEY (`inventory_id`),
  KEY `fk_inventory_menu_item1_idx` (`menu_item_id`),
  CONSTRAINT `fk_inventory_menu_item1` FOREIGN KEY (`menu_item_id`) REFERENCES `menu_item` (`menu_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory`
--

LOCK TABLES `inventory` WRITE;
/*!40000 ALTER TABLE `inventory` DISABLE KEYS */;
/*!40000 ALTER TABLE `inventory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `menu_item`
--

DROP TABLE IF EXISTS `menu_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `menu_item` (
  `menu_item_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `category_id` int NOT NULL,
  `status` enum('Available','Out of stock') NOT NULL DEFAULT 'Available',
  `image` varchar(225) DEFAULT NULL,
  `created_at` timestamp NOT NULL,
  `created_by` int NOT NULL,
  `is_deleted` tinyint NOT NULL,
  `deleted_at` datetime DEFAULT NULL,
  `deleted_by` int DEFAULT NULL,
  `updated_at` date DEFAULT NULL,
  `updated_by` int DEFAULT NULL,
  PRIMARY KEY (`menu_item_id`),
  KEY `fk_menu_item_category1_idx` (`category_id`),
  CONSTRAINT `fk_menu_item_category1` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `menu_item`
--

LOCK TABLES `menu_item` WRITE;
/*!40000 ALTER TABLE `menu_item` DISABLE KEYS */;
INSERT INTO `menu_item` VALUES (1,'fried rice',8500.00,1,'Available','fried-rice.jpg','2026-03-11 06:30:00',1,0,NULL,NULL,NULL,NULL),(2,'tom yum soup',9000.00,2,'Available','tom-yam-soup.jpg','2026-03-11 06:30:00',1,0,NULL,NULL,NULL,NULL),(3,'water',1500.00,3,'Available','water.jpg','2026-03-11 06:30:00',1,0,NULL,NULL,NULL,NULL),(4,'chocolate ice-cream',3000.00,4,'Available','chocolate-icecream.jpg','2026-03-11 06:30:00',1,0,NULL,NULL,NULL,NULL),(5,'Cheese Cake',6500.00,4,'Available','bratz jade.jpg','2026-04-13 04:58:16',15,0,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `menu_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order`
--

DROP TABLE IF EXISTS `order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order` (
  `order_id` int NOT NULL AUTO_INCREMENT,
  `order_date` datetime NOT NULL,
  `order_type` varchar(10) NOT NULL,
  `status` enum('Paid','Pending','Cancelled') NOT NULL DEFAULT 'Pending',
  `customer_id` int DEFAULT NULL,
  `restaurant_table_id` int NOT NULL,
  `user_id` int NOT NULL,
  `tax` decimal(5,2) NOT NULL,
  `service_charge` decimal(5,2) NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  PRIMARY KEY (`order_id`),
  KEY `fk_order_restaurant_table1_idx` (`restaurant_table_id`),
  KEY `fk_order_staff1_idx` (`user_id`),
  KEY `fk_order_customer1_idx` (`customer_id`),
  CONSTRAINT `fk_order_customer1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`),
  CONSTRAINT `fk_order_restaurant_table1` FOREIGN KEY (`restaurant_table_id`) REFERENCES `restaurant_table` (`restaurant_table_id`),
  CONSTRAINT `fk_order_staff1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order`
--

LOCK TABLES `order` WRITE;
/*!40000 ALTER TABLE `order` DISABLE KEYS */;
INSERT INTO `order` VALUES (1,'2026-04-10 22:00:54','Dine-in','Pending',NULL,4,23,0.00,0.00,0.00),(2,'2026-04-11 13:31:49','Dine-in','Pending',NULL,5,23,0.00,0.00,0.00),(3,'2026-04-13 01:14:16','Dine-in','Pending',NULL,6,23,0.00,0.00,0.00),(4,'2026-04-13 01:23:53','Dine-in','Pending',NULL,5,23,0.00,0.00,0.00),(5,'2026-04-13 10:08:23','Dine-in','Pending',NULL,4,23,0.00,0.00,0.00),(6,'2026-04-13 11:12:48','Dine-in','Pending',NULL,5,23,0.00,0.00,8500.00),(7,'2026-04-13 20:32:25','Dine-in','Pending',NULL,4,23,0.00,0.00,21000.00),(8,'2026-04-13 20:33:52','Dine-in','Pending',NULL,4,23,0.00,0.00,27000.00);
/*!40000 ALTER TABLE `order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_item`
--

DROP TABLE IF EXISTS `order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item` (
  `order_item_id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `menu_item_id` int NOT NULL,
  `note` text,
  `quantity` int NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `total` decimal(10,2) NOT NULL,
  `item_status` enum('Pending','Cooking','Ready','Served') NOT NULL DEFAULT 'Pending',
  PRIMARY KEY (`order_item_id`),
  KEY `fk_order_item_menu_item1_idx` (`menu_item_id`),
  KEY `fk_order_item_order1_idx` (`order_id`),
  CONSTRAINT `fk_order_item_menu_item1` FOREIGN KEY (`menu_item_id`) REFERENCES `menu_item` (`menu_item_id`),
  CONSTRAINT `fk_order_item_order1` FOREIGN KEY (`order_id`) REFERENCES `order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item`
--

LOCK TABLES `order_item` WRITE;
/*!40000 ALTER TABLE `order_item` DISABLE KEYS */;
INSERT INTO `order_item` VALUES (1,6,1,'',1,8500.00,8500.00,'Pending'),(2,7,4,'',7,3000.00,21000.00,'Pending'),(3,8,2,'',3,9000.00,27000.00,'Pending');
/*!40000 ALTER TABLE `order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment`
--

DROP TABLE IF EXISTS `payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment` (
  `payment_id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `total_amount` decimal(10,2) DEFAULT NULL,
  `point_discount` decimal(10,2) DEFAULT NULL,
  `final_amount` decimal(10,2) DEFAULT NULL,
  `payment_method` varchar(10) NOT NULL,
  `transaction_date` datetime NOT NULL,
  `status` enum('Success','Pending','Failed') DEFAULT 'Pending',
  `point_used` int DEFAULT NULL,
  `point_balance` int DEFAULT NULL,
  `sale_report_report_id` int NOT NULL,
  PRIMARY KEY (`payment_id`),
  KEY `fk_payment_order1_idx` (`order_id`),
  CONSTRAINT `fk_payment_order1` FOREIGN KEY (`order_id`) REFERENCES `order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment`
--

LOCK TABLES `payment` WRITE;
/*!40000 ALTER TABLE `payment` DISABLE KEYS */;
/*!40000 ALTER TABLE `payment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `point`
--

DROP TABLE IF EXISTS `point`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `point` (
  `point_id` int NOT NULL AUTO_INCREMENT,
  `customer_id` int NOT NULL,
  `current_point` int NOT NULL,
  PRIMARY KEY (`point_id`),
  KEY `fk_point_customer1_idx` (`customer_id`),
  CONSTRAINT `fk_point_customer1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `point`
--

LOCK TABLES `point` WRITE;
/*!40000 ALTER TABLE `point` DISABLE KEYS */;
/*!40000 ALTER TABLE `point` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservation`
--

DROP TABLE IF EXISTS `reservation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservation` (
  `reservation_id` int NOT NULL AUTO_INCREMENT,
  `customer_id` int NOT NULL,
  `restaurant_table_id` int NOT NULL,
  `reservation_date` date NOT NULL,
  `start_time` time DEFAULT NULL,
  `end_time` time DEFAULT NULL,
  `number_of_people` int DEFAULT NULL,
  `status` enum('Reserved','Occupied') DEFAULT 'Reserved',
  `user_id` int NOT NULL,
  PRIMARY KEY (`reservation_id`),
  KEY `fk_reservation_customer1_idx` (`customer_id`),
  KEY `fk_reservation_restaurant_table1_idx` (`restaurant_table_id`),
  KEY `fk_reservation_user1_idx` (`user_id`),
  CONSTRAINT `fk_reservation_customer1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`),
  CONSTRAINT `fk_reservation_restaurant_table1` FOREIGN KEY (`restaurant_table_id`) REFERENCES `restaurant_table` (`restaurant_table_id`),
  CONSTRAINT `fk_reservation_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservation`
--

LOCK TABLES `reservation` WRITE;
/*!40000 ALTER TABLE `reservation` DISABLE KEYS */;
/*!40000 ALTER TABLE `reservation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `restaurant_table`
--

DROP TABLE IF EXISTS `restaurant_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `restaurant_table` (
  `restaurant_table_id` int NOT NULL AUTO_INCREMENT,
  `table_number` int NOT NULL,
  `status` enum('Available','Occupied','Booked') NOT NULL DEFAULT 'Available',
  `area_id` int NOT NULL,
  `reservation_id` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `created_by` int DEFAULT NULL,
  `is_deleted` tinyint DEFAULT NULL,
  `deleted_at` timestamp NULL DEFAULT NULL,
  `deleted_by` int DEFAULT NULL,
  `item_status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`restaurant_table_id`),
  KEY `fk_restaurant_table_area_idx` (`area_id`),
  CONSTRAINT `fk_restaurant_table_area` FOREIGN KEY (`area_id`) REFERENCES `area` (`area_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `restaurant_table`
--

LOCK TABLES `restaurant_table` WRITE;
/*!40000 ALTER TABLE `restaurant_table` DISABLE KEYS */;
INSERT INTO `restaurant_table` VALUES (1,101,'Available',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(2,201,'Available',2,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(3,301,'Available',3,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(4,105,'Available',4,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(5,106,'Available',4,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(6,108,'Available',4,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `restaurant_table` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `role_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (1,'Admin'),(2,'Waiter'),(3,'Chef'),(4,'Cashier');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `user_name` varchar(45) NOT NULL,
  `password` varchar(300) NOT NULL,
  `email` varchar(100) NOT NULL,
  `role_id` int NOT NULL,
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0 = active, 1 = suspended',
  `failed_login_attempts` int NOT NULL DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` int DEFAULT NULL,
  `is_deleted` tinyint DEFAULT '0',
  `deleted_at` timestamp NULL DEFAULT NULL,
  `deleted_by` int DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updated_by` int DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  KEY `fk_staff_role1_idx` (`role_id`),
  CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'Admin','$2a$10$6jtJSYTGZGskiPLK9IemSu/ERMlhF8B8zpjEcAtXU0Z/PNvF.rwxO','admin@gmail.com',1,0,0,'2026-03-10 07:30:00',1,1,'2026-04-07 15:39:47',1,'2026-04-07 15:39:47',1),(5,'Bo Bo','$2a$10$7Pbc8LuL9.S/19S.n6ZkUuO.qXoEaFhT.Gv8N8U/vK6j.qN.E/uLu','Bo11@gmail.com',2,0,0,'2026-04-03 09:38:14',NULL,1,'2026-04-07 16:17:26',13,'2026-04-07 16:17:26',13),(6,'Hla Hla','$2a$10$4D5z4RPoLpDsu..z3Jr8/uYfyVy83lIdKIarnCtN1wPgxbEEMdtg2','hla@gmail.com',3,0,0,'2026-04-05 16:56:08',1,1,'2026-04-05 16:57:36',NULL,NULL,NULL),(7,'Hla Hla ','$2a$10$b0PnTPyd0r3LjqyjYKUy5OEwL.77cQvt6Zk1hC4tEmSXxorz7tG6e','hla@gmail.com',3,0,0,'2026-04-05 17:00:01',1,1,'2026-04-05 17:02:40',NULL,NULL,NULL),(8,'Hla Hla ','$2a$10$uXbTpdM913V3gsqSVqR0QuiUi8Vb1IHhuA/bnQUkatKcp4UI3tesO','hla@gmail.com',3,0,0,'2026-04-05 17:02:53',1,1,'2026-04-05 17:04:41',NULL,NULL,NULL),(9,'Hla Hla ','$2a$10$rouN4DcPaQ47NALXGwLDROWqzwYjAVT29JJGiwmwUTPwtnNTIvZ6m','hla@gmail.com',1,1,0,'2026-04-05 17:04:51',1,1,'2026-04-07 16:17:23',13,'2026-04-07 16:17:23',13),(10,'Bo Bo','$2a$10$zHzYh1DjZkDm.t3xdEPwHerG42NUZ5ZI1pPo92A6cJ5tdT.WA8do.','bobo@gmail.com',2,0,0,'2026-04-07 11:25:31',1,1,'2026-04-07 12:25:55',NULL,'2026-04-07 12:16:51',NULL),(11,'BoBo','$2a$10$adetL6Ok771rnZNCsQKQ3.4armcxGNl2NQhNIRIAClgyJ.P4uM302','Bo11@gmail.com',2,0,0,'2026-04-07 15:25:02',1,1,'2026-04-07 15:25:55',NULL,NULL,NULL),(12,'PaPa','$2a$10$bH55b9hVaPsCCybIhxYyuueBTFPNgRuxOlrbPW2ifK7NCAjDEzXSO','Pa@gmail.com',2,0,0,'2026-04-07 15:39:42',1,1,'2026-04-07 16:17:19',13,'2026-04-07 16:17:19',13),(13,'Admin','$2a$10$6jtJSYTGZGskiPLK9IemSu/ERMlhF8B8zpjEcAtXU0Z/PNvF.rwxO','admin@gmail.com',1,0,0,'2026-04-07 16:15:31',NULL,1,'2026-04-07 16:17:16',13,'2026-04-07 16:17:16',13),(14,'Hla Hla','$2a$10$vmzPoTYGKExo6a8XiT4vSeRh7SRvmA13O/GS22BMC1CnIaoKds3qK','hla@gmail.com',2,0,0,'2026-04-07 16:18:19',13,0,NULL,NULL,'2026-04-07 16:18:19',NULL),(15,'Yar Yar','$2a$10$eH6PyYtl1iH.oITuKKbCmuMObjWojt457GPzz0fEQEo51jcYVOqO.','yar@gmail.com',1,0,0,'2026-04-07 16:24:19',13,0,NULL,NULL,'2026-04-07 16:24:19',NULL),(16,'Kyaw Kyaw','$2a$10$LtBXpu2Zf6yky1lbwCb4FuBWdJxRq8Y9Zqs6MOrodFkJJuszFQ7qe','kyaw@gmail.com',3,1,0,'2026-04-07 16:25:05',13,0,NULL,NULL,'2026-04-07 16:25:19',13),(17,'Po Po','$2a$10$3G.2geS5z5i9VDHIL0iP6.Lngz34FgyDUSRXJ1.wuPyL.FjSU/w0y','Po@gmail.com',4,0,0,'2026-04-07 16:28:38',13,1,'2026-04-07 16:28:42',13,'2026-04-07 16:28:42',13),(18,'SuSu','$2a$10$na7Fa9uYJiHJhZBpi0wFXOl2R2ui3SGdDS4D7naE24rSYudVfXuxq','Su11@gmail.com',2,0,0,'2026-04-07 16:38:51',15,0,NULL,NULL,'2026-04-07 17:57:29',15),(19,'Mya  Moe','$2a$10$h0gMzqCzOrbemqk1m3DtOeo5QYg9eK/JkAjWgzyFJadJfUFkn28vW','moe@gmail.com',4,1,0,'2026-04-07 18:43:39',15,0,NULL,NULL,'2026-04-07 18:47:28',15),(20,'NiNi','$2a$10$PcEcpdInw1.kG/ALqDZ0NeBAEGcJuV9YRzOY7zT09IV6Mw3rSBy8a','Ni@gmail.com',3,0,0,'2026-04-07 18:47:23',15,0,NULL,NULL,'2026-04-07 18:47:23',NULL),(21,'MaMa','$2a$10$4AQ459geo98BEq5SWzf2EeeD8T/nOkpJ6CvUVK3lbKpuB3JcsIEse','ma@gmail.com',2,0,0,'2026-04-07 18:47:54',15,0,NULL,NULL,'2026-04-07 18:47:54',NULL),(22,'PhyuPhyu','$2a$10$SBg4DBwSQ9zDX1RsQkxmSerH03QD5kdUXiWDZL4RcWo.MUV24of1C','phyu@gmail.com',2,0,0,'2026-04-07 18:48:20',15,0,NULL,NULL,'2026-04-07 18:48:20',NULL),(23,'Ye Ye','$2a$10$jnbsSqU5353Az3zlIbHK2uF6mbPqkmvx7ziyq3qFe.EDb/eHtG6Uu','ye@gmail.com',2,0,0,'2026-04-07 18:48:44',15,0,NULL,NULL,'2026-04-07 19:34:10',15),(24,'Mg Phone','$2a$10$3lgTauy9Qp8RpnX2p3LU3eTJIBQ8/qC.wvE.Cm1iONAq1IryN72W.','ye@gmail.com',2,0,0,'2026-04-07 18:57:59',15,1,'2026-04-07 18:59:05',15,'2026-04-07 18:59:05',15);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-17 18:17:09
