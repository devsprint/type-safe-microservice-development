package com.typesafedev.tsmd

import com.typesafedev.tsmd.TS001__Introduction.HostName.assert
import zio.prelude.Assertion.{greaterThan, greaterThanOrEqualTo, hasLength, lessThan}
import zio.prelude.{Newtype, Subtype}

object TS001__Introduction {

  //A common situation in domain modeling is that we have multiple types in our business domain with the same underlying representation in Scala.

//  final case class ComputerSystem(hostName: String, ip: String, os: String, memorySize: Long)

  // option 1
//  type HostName = String
//  type IP = String
//  type OS = String
//  type MemorySize = Long
////
//  // this option still do not help much as you can still use IP for hostName or os
  //final case class ComputerSystem(hostName: HostName, ip: IP, os: OS, memorySize: MemorySize)
//
  // option 2
//  final case class HostName(value: String)
//  final case class IP(value: String)
//  final case class OS(value: String)
//  final case class MemorySize(value: Long)
//  final case class ComputerSystem(hostName: HostName, ip: IP, os: OS, memorySize: MemorySize)

  // This is an improvement in type safety because hostName, ip, os, memorySize are now separate types and compiler will generate errors if we are not using the correct types.
  // However, it is coming with its own cost. Every one of these new types will now allocate an additional object which can add up over the course of a large application with a complex domain model.
  // We can try to minimize these allocations using techniques such as extending AnyVal, but these approaches can be extremely fragile and can actually result in worse performance than the original code if we are not careful

  // option 3
  // There are libraries in Scala ecosystem that help us to better define types like refined or zio-prelude New Types
  // A new type in ZIO Prelude is a type that has the same underlying representation as another type at runtime but is a separate type at compile time.
//  object HostName extends Newtype[String]
//  type HostName = HostName.Type
//  object IP extends Newtype[String]
//  type IP = IP.Type
//  object OS extends Newtype[String]
//  type OS = OS.Type
//  object MemorySize extends Newtype[Long]
//  type MemorySize = MemorySize.Type
//  final case class ComputerSystem(hostName: HostName, ip: IP, os: OS, memorySize: MemorySize)

  // option 4
  // So far, all the new types we have created have been distinct from the underlying types but have not imposed any additional constraints on the values that the underlying type can take.
  object HostName extends Subtype[String] {
    override def assertion = assert {
      hasLength(greaterThan(0)) && hasLength(lessThan(255))
    }
  }
  type HostName = HostName.Type

  val hostName = HostName("test")
  //val hostNameEmpty = HostName("")

//
//  Newtype Assertion Failed
//  assertion = hasLength(greaterThan(0)) && hasLength(lessThan(255))
//
//  •  did not satisfy hasLength(greaterThan(0))

  //val hostNameEmpty = HostName("")

  sealed trait IP
  object IP {
    object IPv4 extends Subtype[String] with IP {
      override def assertion = assert {
        hasLength(greaterThan(7)) && hasLength(lessThan(15))
      }
    }
    object IPv6 extends Subtype[String] with IP {
      override def assertion = assert {
        hasLength(greaterThan(4)) && hasLength(lessThan(45))
      }
    }
    type IPV6 = IPv6.Type
    type IPV4 = IPv4.Type
  }

  final case class OS( name: String, version: String)

  object MemorySize extends Subtype[Long] {
    greaterThanOrEqualTo(0)
  }

  type MemorySize = MemorySize.Type

  final case class ComputerSystem(hostName: HostName, ip: IP, os: OS, memorySize: MemorySize)


}
