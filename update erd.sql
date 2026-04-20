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
INSERT INTO `menu_item` VALUES (1,'fried rice',8500.00,1,'Available','fried-rice.jpg','2026-03-11 06:30:00',1,0,NULL,NULL,NULL,NULL),(2,'tom yum soup',9000.00,2,'Available','tom-yam-soup.jpg','2026-03-11 06:30:00',1,0,NULL,NULL,NULL,NULL),(3,'water',1500.00,3,'Available','water.jpg','2026-03-11 06:30:00',1,0,NULL,NULL,NULL,NULL),(4,'chocolate ice-cream',3000.00,4,'Available','chocolate-icecream.jpg','2026-03-11 06:30:00',1,0,NULL,NULL,NULL,NULL),(5,'Cheese Cake',6500.00,4,'Available','bratz jade.jpg','2026-04-13 04:58:16',15,0,NULL,NULL,'2026-04-19',15);
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
  `order_type` enum('Dine-in','Other') NOT NULL DEFAULT 'Dine-in',
  `status` varchar(50) DEFAULT NULL,
  `restaurant_table_id` int NOT NULL,
  `user_id` int NOT NULL,
  `created_by` int NOT NULL,
  `tax` decimal(12,2) DEFAULT NULL,
  `service_charge` decimal(12,2) DEFAULT NULL,
  `total_amount` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`order_id`),
  KEY `fk_order_staff1_idx` (`user_id`),
  KEY `fk_order_created_by_user` (`created_by`),
  KEY `fk_order_restaurant_table_id_idx` (`restaurant_table_id`),
  CONSTRAINT `fk_order_created_by_user` FOREIGN KEY (`created_by`) REFERENCES `user` (`user_id`),
  CONSTRAINT `fk_order_restaurant_table_id` FOREIGN KEY (`restaurant_table_id`) REFERENCES `restaurant_table` (`restaurant_table_id`),
  CONSTRAINT `fk_order_staff1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order`
--

LOCK TABLES `order` WRITE;
/*!40000 ALTER TABLE `order` DISABLE KEYS */;
INSERT INTO `order` VALUES (18,'2026-04-20 02:12:18','Dine-in','Paid',5,23,23,525.00,210.00,11235.00),(19,'2026-04-20 02:30:33','Dine-in','Paid',4,23,23,500.00,200.00,10700.00),(20,'2026-04-20 02:31:05','Dine-in','Paid',6,23,23,300.00,120.00,6420.00),(21,'2026-04-20 02:51:40','Dine-in','Paid',4,23,23,425.00,170.00,9095.00),(22,'2026-04-20 02:51:51','Dine-in','Paid',9,23,23,525.00,210.00,11235.00),(23,'2026-04-20 03:04:42','Dine-in','Paid',4,23,23,425.00,170.00,9095.00),(24,'2026-04-20 03:04:53','Dine-in','Paid',5,23,23,450.00,180.00,9630.00),(25,'2026-04-20 10:22:09','Dine-in','Paid',4,23,23,425.00,170.00,9095.00),(26,'2026-04-20 10:22:23','Dine-in','Paid',5,23,23,525.00,210.00,11235.00);
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
  `item_status` enum('Pending','Accepted','Cooked','Served','Paid') NOT NULL DEFAULT 'Pending',
  `status_order` tinyint GENERATED ALWAYS AS ((case `item_status` when _utf8mb3'WAITING' then 1 when _utf8mb3'ACCEPTED' then 2 when _utf8mb3'COOKED' then 3 when _utf8mb3'SERVED' then 4 when _utf8mb3'PAID' then 5 else 0 end)) STORED,
  PRIMARY KEY (`order_item_id`),
  KEY `fk_order_item_menu_item1_idx` (`menu_item_id`),
  KEY `fk_order_item_order1_idx` (`order_id`),
  CONSTRAINT `fk_order_item_menu_item1` FOREIGN KEY (`menu_item_id`) REFERENCES `menu_item` (`menu_item_id`),
  CONSTRAINT `fk_order_item_order1` FOREIGN KEY (`order_id`) REFERENCES `order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=137 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item`
--

LOCK TABLES `order_item` WRITE;
/*!40000 ALTER TABLE `order_item` DISABLE KEYS */;
INSERT INTO `order_item` (`order_item_id`, `order_id`, `menu_item_id`, `note`, `quantity`, `unit_price`, `total`, `item_status`) VALUES (124,18,2,'',1,9000.00,9000.00,'Served'),(125,18,3,'',1,1500.00,1500.00,'Served'),(126,19,1,'',1,8500.00,8500.00,'Served'),(127,20,4,'',2,3000.00,6000.00,'Served'),(128,19,3,'m',1,1500.00,1500.00,'Served'),(129,21,1,'ww',1,8500.00,8500.00,'Served'),(130,22,2,'',1,9000.00,9000.00,'Served'),(131,22,3,'',1,1500.00,1500.00,'Served'),(132,23,1,'',1,8500.00,8500.00,'Served'),(133,24,2,'',1,9000.00,9000.00,'Served'),(134,25,1,'',1,8500.00,8500.00,'Served'),(135,26,2,'',1,9000.00,9000.00,'Served'),(136,26,3,'',1,1500.00,1500.00,'Served');
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
  `final_amount` decimal(10,2) DEFAULT NULL,
  `total_amount` decimal(10,2) DEFAULT NULL,
  `payment_method` varchar(10) NOT NULL,
  `transaction_date` datetime NOT NULL,
  `status` enum('Success','Pending','Failed') DEFAULT 'Pending',
  `sale_report_report_id` int NOT NULL,
  `subtotal` decimal(10,2) NOT NULL DEFAULT '0.00',
  `tax` decimal(10,2) NOT NULL DEFAULT '0.00',
  `service_charge` decimal(10,2) NOT NULL DEFAULT '0.00',
  `grand_total` decimal(10,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`payment_id`),
  KEY `fk_payment_order1_idx` (`order_id`),
  CONSTRAINT `fk_payment_order1` FOREIGN KEY (`order_id`) REFERENCES `order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment`
--

LOCK TABLES `payment` WRITE;
/*!40000 ALTER TABLE `payment` DISABLE KEYS */;
INSERT INTO `payment` VALUES (20,19,10700.00,NULL,'Cash','2026-04-20 02:33:40','Success',23,10000.00,500.00,200.00,10700.00),(21,20,6420.00,NULL,'Cash','2026-04-20 02:39:10','Success',24,6000.00,300.00,120.00,6420.00),(22,21,9095.00,NULL,'Cash','2026-04-20 02:54:13','Success',25,8500.00,425.00,170.00,9095.00),(23,22,11235.00,NULL,'Mobile','2026-04-20 02:54:36','Success',26,10500.00,525.00,210.00,11235.00),(24,23,9095.00,NULL,'Cash','2026-04-20 03:06:15','Success',27,8500.00,425.00,170.00,9095.00),(25,24,9630.00,NULL,'Cash','2026-04-20 03:06:26','Success',28,9000.00,450.00,180.00,9630.00),(26,25,9095.00,NULL,'Cash','2026-04-20 10:27:13','Success',29,8500.00,425.00,170.00,9095.00),(27,26,11235.00,NULL,'Card','2026-04-20 10:27:35','Success',30,10500.00,525.00,210.00,11235.00);
/*!40000 ALTER TABLE `payment` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `restaurant_table`
--

LOCK TABLES `restaurant_table` WRITE;
/*!40000 ALTER TABLE `restaurant_table` DISABLE KEYS */;
INSERT INTO `restaurant_table` VALUES (1,101,'Available',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(2,201,'Available',2,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(3,301,'Available',3,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(4,105,'Available',4,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(5,106,'Available',4,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(6,108,'Available',4,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(7,110,'Available',4,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(8,111,'Available',4,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(9,204,'Available',5,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
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
-- Table structure for table `sale_report`
--

DROP TABLE IF EXISTS `sale_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sale_report` (
  `report_id` int NOT NULL AUTO_INCREMENT,
  `report_date` date DEFAULT NULL,
  `total_sales` decimal(10,2) DEFAULT NULL,
  `total_orders` int DEFAULT NULL,
  `created_by` int DEFAULT NULL,
  PRIMARY KEY (`report_id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sale_report`
--

LOCK TABLES `sale_report` WRITE;
/*!40000 ALTER TABLE `sale_report` DISABLE KEYS */;
INSERT INTO `sale_report` VALUES (13,'2026-04-20',11235.00,1,19),(14,'2026-04-20',9630.00,1,19),(15,'2026-04-20',9095.00,1,19),(17,'2026-04-20',19260.00,1,19),(18,'2026-04-20',13375.00,1,19),(19,'2026-04-20',11235.00,1,19),(21,'2026-04-20',11235.00,1,19),(22,'2026-04-20',25680.00,1,19),(23,'2026-04-20',10700.00,1,19),(24,'2026-04-20',6420.00,1,19),(25,'2026-04-20',9095.00,1,19),(26,'2026-04-20',11235.00,1,19),(27,'2026-04-20',9095.00,1,19),(28,'2026-04-20',9630.00,1,19),(29,'2026-04-20',9095.00,1,19),(30,'2026-04-20',11235.00,1,19);
/*!40000 ALTER TABLE `sale_report` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'Admin','$2a$10$6jtJSYTGZGskiPLK9IemSu/ERMlhF8B8zpjEcAtXU0Z/PNvF.rwxO','admin@gmail.com',1,0,0,'2026-03-10 07:30:00',1,1,'2026-04-07 15:39:47',1,'2026-04-07 15:39:47',1),(5,'Bo Bo','$2a$10$7Pbc8LuL9.S/19S.n6ZkUuO.qXoEaFhT.Gv8N8U/vK6j.qN.E/uLu','Bo11@gmail.com',2,0,0,'2026-04-03 09:38:14',NULL,1,'2026-04-07 16:17:26',13,'2026-04-07 16:17:26',13),(6,'Hla Hla','$2a$10$4D5z4RPoLpDsu..z3Jr8/uYfyVy83lIdKIarnCtN1wPgxbEEMdtg2','hla@gmail.com',3,0,0,'2026-04-05 16:56:08',1,1,'2026-04-05 16:57:36',NULL,NULL,NULL),(7,'Hla Hla ','$2a$10$b0PnTPyd0r3LjqyjYKUy5OEwL.77cQvt6Zk1hC4tEmSXxorz7tG6e','hla@gmail.com',3,0,0,'2026-04-05 17:00:01',1,1,'2026-04-05 17:02:40',NULL,NULL,NULL),(8,'Hla Hla ','$2a$10$uXbTpdM913V3gsqSVqR0QuiUi8Vb1IHhuA/bnQUkatKcp4UI3tesO','hla@gmail.com',3,0,0,'2026-04-05 17:02:53',1,1,'2026-04-05 17:04:41',NULL,NULL,NULL),(9,'Hla Hla ','$2a$10$rouN4DcPaQ47NALXGwLDROWqzwYjAVT29JJGiwmwUTPwtnNTIvZ6m','hla@gmail.com',1,1,0,'2026-04-05 17:04:51',1,1,'2026-04-07 16:17:23',13,'2026-04-07 16:17:23',13),(10,'Bo Bo','$2a$10$zHzYh1DjZkDm.t3xdEPwHerG42NUZ5ZI1pPo92A6cJ5tdT.WA8do.','bobo@gmail.com',2,0,0,'2026-04-07 11:25:31',1,1,'2026-04-07 12:25:55',NULL,'2026-04-07 12:16:51',NULL),(11,'BoBo','$2a$10$adetL6Ok771rnZNCsQKQ3.4armcxGNl2NQhNIRIAClgyJ.P4uM302','Bo11@gmail.com',2,0,0,'2026-04-07 15:25:02',1,1,'2026-04-07 15:25:55',NULL,NULL,NULL),(12,'PaPa','$2a$10$bH55b9hVaPsCCybIhxYyuueBTFPNgRuxOlrbPW2ifK7NCAjDEzXSO','Pa@gmail.com',2,0,0,'2026-04-07 15:39:42',1,1,'2026-04-07 16:17:19',13,'2026-04-07 16:17:19',13),(13,'Admin','$2a$10$6jtJSYTGZGskiPLK9IemSu/ERMlhF8B8zpjEcAtXU0Z/PNvF.rwxO','admin@gmail.com',1,0,0,'2026-04-07 16:15:31',NULL,1,'2026-04-07 16:17:16',13,'2026-04-07 16:17:16',13),(14,'Hla Hla','$2a$10$vmzPoTYGKExo6a8XiT4vSeRh7SRvmA13O/GS22BMC1CnIaoKds3qK','hla@gmail.com',2,0,0,'2026-04-07 16:18:19',13,0,NULL,NULL,'2026-04-07 16:18:19',NULL),(15,'Yar Yar','$2a$10$eH6PyYtl1iH.oITuKKbCmuMObjWojt457GPzz0fEQEo51jcYVOqO.','yar@gmail.com',1,0,0,'2026-04-07 16:24:19',13,0,NULL,NULL,'2026-04-07 16:24:19',NULL),(16,'Kyaw Kyaw','$2a$10$LtBXpu2Zf6yky1lbwCb4FuBWdJxRq8Y9Zqs6MOrodFkJJuszFQ7qe','kyaw@gmail.com',3,0,0,'2026-04-07 16:25:05',13,0,NULL,NULL,'2026-04-19 18:17:26',15),(17,'Po Po','$2a$10$3G.2geS5z5i9VDHIL0iP6.Lngz34FgyDUSRXJ1.wuPyL.FjSU/w0y','Po@gmail.com',4,0,0,'2026-04-07 16:28:38',13,1,'2026-04-07 16:28:42',13,'2026-04-07 16:28:42',13),(18,'SuSu','$2a$10$na7Fa9uYJiHJhZBpi0wFXOl2R2ui3SGdDS4D7naE24rSYudVfXuxq','su11@gmail.com',4,0,0,'2026-04-07 16:38:51',15,0,NULL,NULL,'2026-04-18 14:43:59',15),(19,'Mya  Moe','$2a$10$h0gMzqCzOrbemqk1m3DtOeo5QYg9eK/JkAjWgzyFJadJfUFkn28vW','moe@gmail.com',4,0,0,'2026-04-07 18:43:39',15,0,NULL,NULL,'2026-04-20 04:05:47',15),(20,'NiNi','$2a$10$PcEcpdInw1.kG/ALqDZ0NeBAEGcJuV9YRzOY7zT09IV6Mw3rSBy8a','Ni@gmail.com',3,0,0,'2026-04-07 18:47:23',15,0,NULL,NULL,'2026-04-07 18:47:23',NULL),(21,'MaMa','$2a$10$4AQ459geo98BEq5SWzf2EeeD8T/nOkpJ6CvUVK3lbKpuB3JcsIEse','ma@gmail.com',2,0,0,'2026-04-07 18:47:54',15,0,NULL,NULL,'2026-04-07 18:47:54',NULL),(22,'PhyuPhyu','$2a$10$SBg4DBwSQ9zDX1RsQkxmSerH03QD5kdUXiWDZL4RcWo.MUV24of1C','phyu@gmail.com',2,0,0,'2026-04-07 18:48:20',15,0,NULL,NULL,'2026-04-07 18:48:20',NULL),(23,'Ye Ye','$2a$10$jnbsSqU5353Az3zlIbHK2uF6mbPqkmvx7ziyq3qFe.EDb/eHtG6Uu','ye@gmail.com',2,0,0,'2026-04-07 18:48:44',15,0,NULL,NULL,'2026-04-07 19:34:10',15),(24,'Mg Phone','$2a$10$3lgTauy9Qp8RpnX2p3LU3eTJIBQ8/qC.wvE.Cm1iONAq1IryN72W.','ye@gmail.com',2,0,0,'2026-04-07 18:57:59',15,1,'2026-04-07 18:59:05',15,'2026-04-07 18:59:05',15),(25,'zai','$2a$10$YGjE1J1ZG.PMMQYXJCySvOiYdb5eV3G28.16BasFiG20g7RFgpCq2','zaw@gmail.com',3,0,0,'2026-04-17 12:03:58',15,0,NULL,NULL,'2026-04-17 12:34:04',15);
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

-- Dump completed on 2026-04-20 10:42:22
