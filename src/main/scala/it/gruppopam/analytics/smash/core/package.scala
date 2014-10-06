package it.gruppopam.analytics.smash

import java.security.MessageDigest

package object core {
  val md5Generator: MessageDigest = MessageDigest.getInstance("MD5")
}
