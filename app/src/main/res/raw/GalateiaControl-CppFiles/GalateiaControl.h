#include "ariaTypedefs.h"
#include "ArMode.h"
#include "ArActionGroups.h"
#include "ArGripper.h"
#include "ArTcpConnection.h"
#include "ArSerialConnection.h"
#include "ArPTZ.h"
#include "ArTCM2.h"

#define SERVERPORT 7777
#define POSITIONPORT 7778

class ArTCM2;
class ArACTS_1_2;
class ArRobotPacket;
class ArSick;
class ArAnalogGyro;

class GalateiaControl : public ArMode
{
public:
  AREXPORT GalateiaControl(ArRobot *robot, const char *name, char key, char key2);
  AREXPORT ~GalateiaControl();
  AREXPORT virtual void userTask(void);
  AREXPORT virtual void activate(void);
  AREXPORT virtual void deactivate(void);
  void controle(void);
  
protected:
  double x, y, th;
  double x2, y2, th2;
  ArSocket positionSock, serverSock, clientSock;
  ArMutex mutex;
  ArRobot *myRobot;
  char myComando;
  
};


