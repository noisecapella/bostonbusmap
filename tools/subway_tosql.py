mbtaUrl = "http://developer.mbta.com/RT_Archive/RealTimeHeavyRailKeys.csv"
mbtaData = """Line,PlatformKey,PlatformName,StationName,PlatformOrder,StartOfLine,EndOfLine,Branch,Direction,stop_id,stop_code,stop_name,stop_desc,stop_lat,stop_lon
Red,RALEN,ALEWIFE NB,ALEWIFE,17,FALSE,TRUE,Trunk,NB,place-alfcl,,Alewife Station,,42.395428,-71.142483
Red,RDAVN,DAVIS NB,DAVIS,16,FALSE,FALSE,Trunk,NB,place-davis,,Davis Station,,42.39674,-71.121815
Red,RDAVS,DAVIS SB,DAVIS,1,TRUE,FALSE,Trunk,SB,place-davis,,Davis Station,,42.39674,-71.121815
Red,RPORN,PORTER NB,PORTER,15,FALSE,FALSE,Trunk,NB,place-portr,,Porter Square Station,,42.3884,-71.119149
Red,RPORS,PORTER SB,PORTER,2,FALSE,FALSE,Trunk,SB,place-portr,,Porter Square Station,,42.3884,-71.119149
Red,RHARN,HARVARD NB,HARVARD,14,FALSE,FALSE,Trunk,NB,place-harsq,,Harvard Square Station,,42.373362,-71.118956
Red,RHARS,HARVARD SB,HARVARD,3,FALSE,FALSE,Trunk,SB,place-harsq,,Harvard Square Station,,42.373362,-71.118956
Red,RCENN,CENTRAL NB,CENTRAL,13,FALSE,FALSE,Trunk,NB,place-cntsq,,Central Square Station,,42.365486,-71.103802
Red,RCENS,CENTRAL SB,CENTRAL,4,FALSE,FALSE,Trunk,SB,place-cntsq,,Central Square Station,,42.365486,-71.103802
Red,RKENN,KENDALL NB,KENDALL,12,FALSE,FALSE,Trunk,NB,place-knncl,,Kendall/MIT Station,,42.36249079,-71.08617653
Red,RKENS,KENDALL SB,KENDALL,5,FALSE,FALSE,Trunk,SB,place-knncl,,Kendall/MIT Station,,42.36249079,-71.08617653
Red,RMGHN,CHARLES MGH NB,CHARLES MGH,11,FALSE,FALSE,Trunk,NB,place-chmnl,,Charles/MGH Station,,42.361166,-71.070628
Red,RMGHS,CHARLES MGH SB,CHARLES MGH,6,FALSE,FALSE,Trunk,SB,place-chmnl,,Charles/MGH Station,,42.361166,-71.070628
Red,RPRKN,PARK NB,PARK,10,FALSE,FALSE,Trunk,NB,place-pktrm,,Park St. Station,,42.35639457,-71.0624242
Red,RPRKS,PARK SB,PARK,7,FALSE,FALSE,Trunk,SB,place-pktrm,,Park St. Station,,42.35639457,-71.0624242
Red,RDTCN,DOWNTOWN CROSSING NB,DOWNTOWN CROSSING,9,FALSE,FALSE,Trunk,NB,place-dwnxg,,Downtown Crossing Station,,42.355518,-71.060225
Red,RDTCS,DOWNTOWN CROSSING SB,DOWNTOWN CROSSING,8,FALSE,FALSE,Trunk,SB,place-dwnxg,,Downtown Crossing Station,,42.355518,-71.060225
Red,RSOUN,SOUTH STATION NB,SOUTH STATION,8,FALSE,FALSE,Trunk,NB,place-sstat,,South Station,,42.352271,-71.055242
Red,RSOUS,SOUTH STATION SB,SOUTH STATION,9,FALSE,FALSE,Trunk,SB,place-sstat,,South Station,,42.352271,-71.055242
Red,RBRON,BROADWAY NB,BROADWAY,7,FALSE,FALSE,Trunk,NB,place-brdwy,,Broadway Station,,42.342622,-71.056967
Red,RBROS,BROADWAY SB,BROADWAY,10,FALSE,FALSE,Trunk,SB,place-brdwy,,Broadway Station,,42.342622,-71.056967
Red,RANDN,ANDREW NB,ANDREW,6,FALSE,FALSE,Trunk,NB,place-andrw,,Andrew Station,,42.330154,-71.057655
Red,RANDS,ANDREW SB,ANDREW,11,FALSE,FALSE,Trunk,SB,place-andrw,,Andrew Station,,42.330154,-71.057655
Red,RJFKN,JFK NB,JFK,5,FALSE,FALSE,Trunk,NB,place-jfkred,,JFK/UMass Station,,42.320685,-71.052391
Red,RJFKS,JFK SB,JFK,12,FALSE,FALSE,Trunk,SB,place-jfkred,,JFK/UMass Station,,42.320685,-71.052391
Red,RSAVN,SAVIN HILL NB,SAVIN HILL,4,FALSE,FALSE,Ashmont,NB,place-shmnl,,Savin Hill Station,,42.31129,-71.053331
Red,RSAVS,SAVIN HILL SB,SAVIN HILL,13,FALSE,FALSE,Ashmont,SB,place-shmnl,,Savin Hill Station,,42.31129,-71.053331
Red,RFIEN,FIELDS CORNER NB,FIELDS CORNER,3,FALSE,FALSE,Ashmont,NB,place-fldcr,,Fields Corner Station,,42.300093,-71.061667
Red,RFIES,FIELDS CORNER SB,FIELDS CORNER,14,FALSE,FALSE,Ashmont,SB,place-fldcr,,Fields Corner Station,,42.300093,-71.061667
Red,RSHAN,SHAWMUT NB,SHAWMUT,2,TRUE,FALSE,Ashmont,NB,place-smmnl,,Shawmut Station,,42.29312583,-71.06573796
Red,RSHAS,SHAWMUT SB,SHAWMUT,15,FALSE,FALSE,Ashmont,SB,place-smmnl,,Shawmut Station,,42.29312583,-71.06573796
Red,RASHS,ASHMONT SB,ASHMONT,16,FALSE,TRUE,Ashmont,SB,place-asmnl,,Ashmont Station,,42.284652,-71.064489
Red,RNQUN,NORTH QUINCY NB,NORTH QUINCY,4,FALSE,FALSE,Braintree,NB,place-nqncy,,North Quincy Station,,42.275275,-71.029583
Red,RNQUS,NORTH QUINCY SB,NORTH QUINCY,13,FALSE,FALSE,Braintree,SB,place-nqncy,,North Quincy Station,,42.275275,-71.029583
Red,RWOLN,WOLLASTON NB,WOLLASTON,3,FALSE,FALSE,Braintree,NB,place-wlsta,,Wollaston Station,,42.2665139,-71.0203369
Red,RWOLS,WOLLASTON SB,WOLLASTON,14,FALSE,FALSE,Braintree,SB,place-wlsta,,Wollaston Station,,42.2665139,-71.0203369
Red,RQUCN,QUINCY CENTER NB,QUINCY CENTER,2,FALSE,FALSE,Braintree,NB,place-qnctr,,Quincy Center Station,,42.251809,-71.005409
Red,RQUCS,QUINCY CENTER SB,QUINCY CENTER,15,FALSE,FALSE,Braintree,SB,place-qnctr,,Quincy Center Station,,42.251809,-71.005409
Red,RQUAN,QUINCY ADAMS NB,QUINCY ADAMS,1,TRUE,FALSE,Braintree,NB,place-qamnl,,Quincy Adams Station,,42.233391,-71.007153
Red,RQUAS,QUINCY ADAMS SB,QUINCY ADAMS,16,FALSE,FALSE,Braintree,SB,place-qamnl,,Quincy Adams Station,,42.233391,-71.007153
Red,RBRAS,BRAINTREE SB,BRAINTREE,17,FALSE,TRUE,Braintree,SB,place-brntn,,Braintree Station,,42.2078543,-71.0011385
Orange,OOAKN,OAK GROVE NB,OAK GROVE,18,FALSE,TRUE,Trunk,NB,place-ogmnl,,Oak Grove Station,,42.43668,-71.071097
Orange,OMALN,MALDEN NB,MALDEN,17,FALSE,FALSE,Trunk,NB,place-mlmnl,,Malden Center Station,,42.426632,-71.07411
Orange,OMALS,MALDEN SB,MALDEN,1,TRUE,FALSE,Trunk,SB,place-mlmnl,,Malden Center Station,,42.426632,-71.07411
Orange,OWELN,WELLINGTON NB,WELLINGTON,16,FALSE,FALSE,Trunk,NB,place-welln,,Wellington Station,,42.40237,-71.077082
Orange,OWELS,WELLINGTON SB,WELLINGTON,2,FALSE,FALSE,Trunk,SB,place-welln,,Wellington Station,,42.40237,-71.077082
Orange,OSULN,SULLIVAN NB,SULLIVAN,15,FALSE,FALSE,Trunk,NB,place-sull,,Sullivan Station,,42.383975,-71.076994
Orange,OSULS,SULLIVAN SB,SULLIVAN,3,FALSE,FALSE,Trunk,SB,place-sull,,Sullivan Station,,42.383975,-71.076994
Orange,OCOMN,COMMUNITY NB,COMMUNITY,14,FALSE,FALSE,Trunk,NB,place-ccmnl,,Community College Station,,42.373622,-71.069533
Orange,OCOMS,COMMUNITY SB,COMMUNITY,4,FALSE,FALSE,Trunk,SB,place-ccmnl,,Community College Station,,42.373622,-71.069533
Orange,ONSTN,NORTH STATION NB,NORTH STATION,13,FALSE,FALSE,Trunk,NB,place-north,,North Station,,42.365577,-71.06129
Orange,ONSTS,NORTH STATION SB,NORTH STATION,5,FALSE,FALSE,Trunk,SB,place-north,,North Station,,42.365577,-71.06129
Orange,OHAYN,HAYMARKET NB,HAYMARKET,12,FALSE,FALSE,Trunk,NB,place-haecl,,Haymarket Station,,42.363021,-71.05829
Orange,OHAYS,HAYMARKET SB,HAYMARKET,6,FALSE,FALSE,Trunk,SB,place-haecl,,Haymarket Station,,42.363021,-71.05829
Orange,OSTNN,STATE NB,STATE,11,FALSE,FALSE,Trunk,NB,place-state,,State St. Station,,42.358978,-71.057598
Orange,OSTSS,STATE SB,STATE,7,FALSE,FALSE,Trunk,SB,place-state,,State St. Station,,42.358978,-71.057598
Orange,ODTNN,DOWNTOWN CROSSING NB,DOWNTOWN CROSSING,10,FALSE,FALSE,Trunk,NB,place-dwnxg,,Downtown Crossing Station,,42.355518,-71.060225
Orange,ODTSS,DOWNTOWN CROSSING SB,DOWNTOWN CROSSING,8,FALSE,FALSE,Trunk,SB,place-dwnxg,,Downtown Crossing Station,,42.355518,-71.060225
Orange,OCHNN,CHINATOWN NB,CHINATOWN,9,FALSE,FALSE,Trunk,NB,place-chncl,,Chinatown Station,,42.352547,-71.062752
Orange,OCHSS,CHINATOWN SB,CHINATOWN,9,FALSE,FALSE,Trunk,SB,place-chncl,,Chinatown Station,,42.352547,-71.062752
Orange,ONEMN,TUFTS MEDICAL CENTER NB,TUFTS MEDICAL CENTER,8,FALSE,FALSE,Trunk,NB,place-nemnl,,Tufts Medical Center Station,,42.349662,-71.063917
Orange,ONEMS,TUFTS MEDICAL CENTER SB,TUFTS MEDICAL CENTER,10,FALSE,FALSE,Trunk,SB,place-nemnl,,Tufts Medical Center Station,,42.349662,-71.063917
Orange,OBACN,BACK BAY NB,BACK BAY,7,FALSE,FALSE,Trunk,NB,place-bbsta,,Back Bay Station,,42.34735,-71.075727
Orange,OBACS,BACK BAY SB,BACK BAY,11,FALSE,FALSE,Trunk,SB,place-bbsta,,Back Bay Station,,42.34735,-71.075727
Orange,OMASN,MASS AVE NB,MASS AVE,6,FALSE,FALSE,Trunk,NB,place-masta,,Massachusetts Ave. Station,,42.341512,-71.083423
Orange,OMASS,MASS AVE SB,MASS AVE,12,FALSE,FALSE,Trunk,SB,place-masta,,Massachusetts Ave. Station,,42.341512,-71.083423
Orange,ORUGN,RUGGLES NB,RUGGLES,5,FALSE,FALSE,Trunk,NB,place-rugg,,Ruggles Station,,42.336377,-71.088961
Orange,ORUGS,RUGGLES SB,RUGGLES,13,FALSE,FALSE,Trunk,SB,place-rugg,,Ruggles Station,,42.336377,-71.088961
Orange,OROXN,ROXBURY NB,ROXBURY,4,FALSE,FALSE,Trunk,NB,place-rcmnl,,Roxbury Crossing Station,,42.331397,-71.095451
Orange,OROXS,ROXBURY SB,ROXBURY,14,FALSE,FALSE,Trunk,SB,place-rcmnl,,Roxbury Crossing Station,,42.331397,-71.095451
Orange,OJACN,JACKSON NB,JACKSON,3,FALSE,FALSE,Trunk,NB,place-jaksn,,Jackson Square Station,,42.323132,-71.099592
Orange,OJACS,JACKSON SB,JACKSON,15,FALSE,FALSE,Trunk,SB,place-jaksn,,Jackson Square Station,,42.323132,-71.099592
Orange,OSTON,STONY BROOK NB,STONY BROOK,2,FALSE,FALSE,Trunk,NB,place-sbmnl,,Stony Brook Station,,42.317062,-71.104248
Orange,OSTOS,STONY BROOK SB,STONY BROOK,16,FALSE,FALSE,Trunk,SB,place-sbmnl,,Stony Brook Station,,42.317062,-71.104248
Orange,OGREN,GREEN STREET NB,GREEN STREET,1,TRUE,FALSE,Trunk,NB,place-grnst,,Green St. Station,,42.310525,-71.107414
Orange,OGRES,GREEN STREET SB,GREEN STREET,17,FALSE,FALSE,Trunk,SB,place-grnst,,Green St. Station,,42.310525,-71.107414
Orange,OFORS,FOREST HILLS SB,FOREST HILLS,18,FALSE,TRUE,Trunk,SB,place-forhl,,Forest Hills Station,,42.300523,-71.113686
Blue,BWONE,WONDERLAND EB,WONDERLAND,12,FALSE,TRUE,Trunk,EB,place-wondl,,Wonderland Station,,42.41342,-70.991648
Blue,BREVE,REVERE EB,REVERE,11,FALSE,FALSE,Trunk,EB,place-rbmnl,,Revere Beach Station,,42.40784254,-70.99253321
Blue,BREVW,REVERE WB,REVERE,1,TRUE,FALSE,Trunk,WB,place-rbmnl,,Revere Beach Station,,42.40784254,-70.99253321
Blue,BBEAE,BEACHMONT EB,BEACHMONT,10,FALSE,FALSE,Trunk,EB,place-bmmnl,,Beachmont Station,,42.39754234,-70.99231944
Blue,BBEAW,BEACHMONT WB,BEACHMONT,2,FALSE,FALSE,Trunk,WB,place-bmmnl,,Beachmont Station,,42.39754234,-70.99231944
Blue,BSUFE,SUFFOLK DOWNS EB,SUFFOLK DOWNS,9,FALSE,FALSE,Trunk,EB,place-sdmnl,,Suffolk Downs Station,,42.39050067,-70.99712259
Blue,BSUFW,SUFFOLK DOWNS WB,SUFFOLK DOWNS,3,FALSE,FALSE,Trunk,WB,place-sdmnl,,Suffolk Downs Station,,42.39050067,-70.99712259
Blue,BORHE,ORIENT HEIGHTS EB,ORIENT HEIGHTS,8,FALSE,FALSE,Trunk,EB,place-orhte,,Orient Heights Station,,42.386867,-71.004736
Blue,BORHW,ORIENT HEIGHTS WB,ORIENT HEIGHTS,4,FALSE,FALSE,Trunk,WB,place-orhte,,Orient Heights Station,,42.386867,-71.004736
Blue,BWOOE,WOOD ISLAND EB,WOOD ISLAND,7,FALSE,FALSE,Trunk,EB,place-wimnl,,Wood Island Station,,42.3796403,-71.02286539
Blue,BWOOW,WOOD ISLAND WB,WOOD ISLAND,5,FALSE,FALSE,Trunk,WB,place-wimnl,,Wood Island Station,,42.3796403,-71.02286539
Blue,BAIRE,AIRPORT EB,AIRPORT,6,FALSE,FALSE,Trunk,EB,place-aport,,Airport Station,,42.374262,-71.030395
Blue,BAIRW,AIRPORT WB,AIRPORT,6,FALSE,FALSE,Trunk,WB,place-aport,,Airport Station,,42.374262,-71.030395
Blue,BMAVE,MAVERICK EB,MAVERICK,5,FALSE,FALSE,Trunk,EB,place-mvbcl,,Maverick Station,,42.36911856,-71.03952958
Blue,BMAVW,MAVERICK WB,MAVERICK,7,FALSE,FALSE,Trunk,WB,place-mvbcl,,Maverick Station,,42.36911856,-71.03952958
Blue,BAQUE,AQUARIUM EB,AQUARIUM,4,FALSE,FALSE,Trunk,EB,place-aqucl,,Aquarium Station,,42.359784,-71.051652
Blue,BAQUW,AQUARIUM WB,AQUARIUM,8,FALSE,FALSE,Trunk,WB,place-aqucl,,Aquarium Station,,42.359784,-71.051652
Blue,BSTAE,STATE EB,STATE,3,FALSE,FALSE,Trunk,EB,place-state,,State St. Station,,42.358978,-71.057598
Blue,BSTAW,STATE WB,STATE,9,FALSE,FALSE,Trunk,WB,place-state,,State St. Station,,42.358978,-71.057598
Blue,BGOVE,GOVERNMENT CENTER EB,GOVERNMENT CENTER,2,FALSE,FALSE,Trunk,EB,place-gover,,Government Center Station,,42.359705,-71.059215
Blue,BGOVW,GOVERNMENT CENTER WB,GOVERNMENT CENTER,10,FALSE,FALSE,Trunk,WB,place-gover,,Government Center Station,,42.359705,-71.059215
Blue,BBOWE,BOWDOIN EB,BOWDOIN,1,TRUE,FALSE,Trunk,EB,place-bomnl,,Bowdoin Station,,42.361365,-71.062037
Blue,BBOWW,BOWDOIN WB,BOWDOIN,11,FALSE,TRUE,Trunk,WB,place-bomnl,,Bowdoin Station,,42.361365,-71.062037
"""
RedNorthToAlewife = "RedNB0"
RedNorthToAlewife2 = "RedNB1"
RedSouthToBraintree = "RedSB0"
RedSouthToAshmont = "RedSB1"
BlueEastToWonderland = "BlueEB0"
BlueWestToBowdoin = "BlueWB0"
OrangeNorthToOakGrove = "OrangeNB0"
OrangeSouthToForestHills = "OrangeSB0"

RedLine = "Red"
OrangeLine = "Orange"
BlueLine = "Blue"

subway_color = {RedLine: 0xff0000,
                OrangeLine: 0xf88017,
                BlueLine: 0x0000ff}

red = 0xff0000

import urllib2
import sys
import schema
import xml.sax
import xml.sax.handler
import routetitleshandler
import argparse


def createDirectionHash(direction, branch):
    return direction + branch

class Direction:
    def __init__(self, name, title, route, useForUI):
        self.name = name
        self.title = title
        self.route = route
        self.useForUI = useForUI

def write_sql(data, routeTitles, startOrder):
    lines = data.split("\n")
    indexes = {}
    first_line = lines[0].strip()
    header_items = first_line.split(",")
    for i in xrange(len(header_items)):
        indexes[header_items[i]] = i
    obj = schema.getSchemaAsObject()
    routes_done = {}
    orderedStations = {}
    paths = {}
    for line in filter(lambda x: x, (line.strip() for line in lines[1:])):
        items = line.split(",")
        routeName = items[indexes["Line"]]
        
        if routeName not in routes_done:
            routeTitle, order = routeTitles[routeName]

            routes_done[routeName] = {"route":routeName,
                                      "routetitle":routeTitle,
                                      "color":subway_color[routeName],
                                      "oppositecolor":subway_color[routeName],
                                      "listorder":startOrder + order,
                                      "agencyid":schema.SubwayAgencyId}


        platformOrder = int(items[indexes["PlatformOrder"]])
        latitudeAsDegrees = float(items[indexes["stop_lat"]])
        longitudeAsDegrees = float(items[indexes["stop_lon"]])
        tag = items[indexes["PlatformKey"]]
        title = items[indexes["stop_name"]]
        branch = items[indexes["Branch"]]

        obj.stops.tag.value = tag
        obj.stops.title.value = title
        obj.stops.lat.value = latitudeAsDegrees
        obj.stops.lon.value = longitudeAsDegrees
        obj.stops.insert()

        obj.subway.platformorder.value = platformOrder
        obj.subway.branch.value = branch
        obj.subway.tag.value = tag
        obj.subway.insert()

        obj.stopmapping.route.value = routeName
        obj.stopmapping.tag.value = tag
        obj.stopmapping.dirTag.value = None
        obj.stopmapping.insert()

        if routeName not in orderedStations:
            orderedStations[routeName] = {}
            paths[routeName] = []
        innerMapping = orderedStations[routeName]

        direction = items[indexes["Direction"]]
        
        combinedDirectionHash = createDirectionHash(direction, branch)
        if combinedDirectionHash not in innerMapping:
            innerMapping[combinedDirectionHash] = {}

        innerInnerMapping = innerMapping[combinedDirectionHash]
        innerInnerMapping[platformOrder] = (latitudeAsDegrees, longitudeAsDegrees)

    # workaround
    directions = {RedNorthToAlewife : Direction("North toward Alewife", "", RedLine, True),
                  RedNorthToAlewife2 : Direction("North toward Alewife", "", RedLine, True),
                  RedSouthToBraintree : Direction("South toward Braintree", "", RedLine, True),
                  RedSouthToAshmont : Direction("South toward Ashmont", "", RedLine, True),
                  BlueEastToWonderland : Direction("East toward Wonderland", "", BlueLine, True),
                  BlueWestToBowdoin : Direction("West toward Bowdoin", "", BlueLine, True),
                  OrangeNorthToOakGrove : Direction("North toward Oak Grove", "", OrangeLine, True),
                  OrangeSouthToForestHills : Direction("South toward Forest Hills", "", OrangeLine, True)}

    for dirKey, direction in directions.iteritems():
        obj.directions.dirTag.value = dirKey
        obj.directions.dirNameKey.value = direction.name
        obj.directions.dirTitleKey.value = direction.title
        obj.directions.dirRouteKey.value = direction.route
        obj.directions.useAsUI.value = schema.getIntFromBool(direction.useForUI)
        obj.directions.insert()
    for route, innerMapping in orderedStations.iteritems():
        for directionHash, stations in innerMapping.iteritems():
            floats = []
            for platformOrder in sorted(stations.keys()):
                floats.append(stations[platformOrder])

            #this is kind of a hack. We need to connect the southern branches of the red line
            if directionHash == "NBAshmont" or directionHash == "NBBraintree":
                jfkNorthBoundOrder = 5
                jfkStation = innerMapping["NBTrunk"][jfkNorthBoundOrder]
                jfkLat, jfkLon = jfkStation
                floats.append(jfkStation)
            paths[route].append(floats)

    for route, routedata in routes_done.iteritems():
        for key, value in routedata.iteritems():
            getattr(obj.routes, key).value = value

        if route in paths:
            obj.routes.pathblob.value = schema.Box(paths[route]).get_blob_string()

        obj.routes.insert()

        

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Parse commuterrail data into SQL')
    parser.add_argument("routeList", type=str)
    parser.add_argument("order", type=int)
    args = parser.parse_args()

    routeTitleParser = xml.sax.make_parser()
    routeHandler = routetitleshandler.RouteTitlesHandler()
    routeTitleParser.setContentHandler(routeHandler)
    routeTitleParser.parse(args.routeList)

    routeTitles = routeHandler.mapping
    
    #response = urllib2.urlopen(mbtaUrl)
    #data = response.read()
    data = mbtaData

    print "BEGIN TRANSACTION;"
    write_sql(data, routeHandler.mapping, args.order)
    
    print "END TRANSACTION;"









