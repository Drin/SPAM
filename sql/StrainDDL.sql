-- phpMyAdmin SQL Dump
-- version 2.11.11.3
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Apr 20, 2013 at 11:45 AM
-- Server version: 5.0.95
-- PHP Version: 5.1.6

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- Database: `CPLOP`
--

-- --------------------------------------------------------

--
-- Table structure for table `Strain`
--

CREATE TABLE IF NOT EXISTS `Strain` (
  `id` int(11) NOT NULL auto_increment,
  `cluster_name` varchar(100) NOT NULL,
  `parent` int(11) default NULL,
  `date` datetime NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `cluster_name` (`cluster_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `StrainFamily`
--

CREATE TABLE IF NOT EXISTS `StrainFamily` (
  `id` int(11) NOT NULL auto_increment,
  `avgDistance` float NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `StrainFamilyToStrainLink`
--

CREATE TABLE IF NOT EXISTS `StrainFamilyToStrainLink` (
  `id` int(11) NOT NULL,
  `strainID` int(11) NOT NULL,
  `strainFamilyID` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `strainID` (`strainID`),
  KEY `strainFamilyID` (`strainFamilyID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `StrainIsolateLink`
--

CREATE TABLE IF NOT EXISTS `StrainIsolateLink` (
  `id` int(11) NOT NULL auto_increment,
  `cluster_id` int(11) NOT NULL,
  `isolate_id` varchar(15) NOT NULL,
  `date_time` datetime NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `Index` (`cluster_id`),
  KEY `Isolate Id Index` (`isolate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `StrainRepPyroprint`
--

CREATE TABLE IF NOT EXISTS `StrainRepPyroprint` (
  `id` int(11) NOT NULL,
  `region` varchar(20) NOT NULL,
  `dispensation_name` varchar(205) default NULL,
  `forward_primer` varchar(40) default NULL,
  `reverse_primer` varchar(40) default NULL,
  `sequence_primer` varchar(40) default NULL,
  `cluster_id` int(11) NOT NULL,
  `date_time` datetime NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `cluster_id` (`cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `StrainFamilyToStrainLink`
--
ALTER TABLE `StrainFamilyToStrainLink`
  ADD CONSTRAINT `StrainFamilyToStrainLink_ibfk_2` FOREIGN KEY (`strainFamilyID`) REFERENCES `StrainFamilyToStrainLink` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `StrainFamilyToStrainLink_ibfk_1` FOREIGN KEY (`strainID`) REFERENCES `Strain` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `StrainIsolateLink`
--
ALTER TABLE `StrainIsolateLink`
  ADD CONSTRAINT `StrainIsolateLink_ibfk_2` FOREIGN KEY (`isolate_id`) REFERENCES `Isolates` (`isoID`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `StrainIsolateLink_ibfk_1` FOREIGN KEY (`cluster_id`) REFERENCES `Strain` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `StrainRepPyroprint`
--
ALTER TABLE `StrainRepPyroprint`
  ADD CONSTRAINT `StrainRepPyroprint_ibfk_1` FOREIGN KEY (`cluster_id`) REFERENCES `Strain` (`id`);

