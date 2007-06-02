/*
   packet types for the RF link between orbs and mothership

   These from SPU meeting 28 May 2007
*/

enum ELinkPacketType {

  eLinkRTCM         = 0x00,   // Binary data
  eLinkMotorControl,
  eLinkArt,
  eLinkNMEA,
  eLinkNavigation,            // Including trajectory and such
  eLinkCCS,                   // Command Control and Status
  eLinkConsole,
  eLinkCodeDownload,
  eLinkLoopback,
  eLinkSonar,
  eLinkLastPacketType
};

