CREATE TABLE `users` (
  `ID` bigint(20) unsigned NOT NULL auto_increment,
  `user_email` varchar(100) NOT NULL default '',
  `user_pass` varchar(64) NOT NULL default '',
  `user_tasks_allow` int(11) NOT NULL default '1',
  `user_type` varchar(16) NOT NULL default 'user',
  PRIMARY KEY  (`ID`),
  UNIQUE (`user_email`)
);