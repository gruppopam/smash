package it.gruppopam.analytics.smash.core


trait RestMessage

case class Facts(urls: Seq[String], params: Map[String, String]) extends RestMessage

case class Fact(endpoint: String, params: Map[String, String])

case class Response(redisKey: String)

case class Error(message: String)

case class Validation(message: String)