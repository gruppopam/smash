package it.gruppopam.analytics.smash.core


trait RestMessage

case class Facts(facts: Array[Fact]) extends RestMessage

case class Fact(url: String, params: Map[String, String])

case class Response(redisKey: String)

case class Error(message: String)

case class Validation(message: String)