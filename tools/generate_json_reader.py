import json
import six

VEHICLE_LOCATIONS = """{
    "mode": [
        {
            "mode_name": "Subway",
            "route": [
                {
                    "direction": [
                        {
                            "direction_id": "0",
                            "direction_name": "Southbound",
                            "trip": [
                                {
                                    "trip_headsign": "Ashmont",
                                    "trip_id": "23850862",
                                    "trip_name": "9:17 pm from Alewife to Ashmont - Outbound",
                                    "vehicle": {
                                        "vehicle_bearing": "260",
                                        "vehicle_id": "1507",
                                        "vehicle_lat": "42.30002",
                                        "vehicle_lon": "-71.06183",
                                        "vehicle_timestamp": "1414721054"
                                    }
                                },
                                {
                                    "trip_headsign": "Ashmont",
                                    "trip_id": "23850861",
                                    "trip_name": "9:29 pm from Alewife to Ashmont - Outbound",
                                    "vehicle": {
                                        "vehicle_bearing": "170",
                                        "vehicle_id": "1730",
                                        "vehicle_lat": "42.31446",
                                        "vehicle_lon": "-71.05228",
                                        "vehicle_timestamp": "1414721095"
                                    }
                                },
                                {
                                    "trip_headsign": "Ashmont",
                                    "trip_id": "23850860",
                                    "trip_name": "9:41 pm from Alewife to Ashmont - Outbound",
                                    "vehicle": {
                                        "vehicle_bearing": "130",
                                        "vehicle_id": "1868",
                                        "vehicle_lat": "42.35648",
                                        "vehicle_lon": "-71.06265",
                                        "vehicle_timestamp": "1414721060"
                                    }
                                },
                                {
                                    "trip_headsign": "Ashmont",
                                    "trip_id": "23850880",
                                    "trip_name": "9:53 pm from Alewife to Ashmont - Outbound",
                                    "vehicle": {
                                        "vehicle_bearing": "135",
                                        "vehicle_id": "1603",
                                        "vehicle_lat": "42.37231",
                                        "vehicle_lon": "-71.11585",
                                        "vehicle_timestamp": "1414721096"
                                    }
                                }
                            ]
                        },
                        {
                            "direction_id": "1",
                            "direction_name": "Northbound",
                            "trip": [
                                {
                                    "trip_headsign": "Alewife",
                                    "trip_id": "23850836",
                                    "trip_name": "9:34 pm from Ashmont - Inbound to Alewife",
                                    "vehicle": {
                                        "vehicle_bearing": "0",
                                        "vehicle_id": "1882",
                                        "vehicle_lat": "42.38416",
                                        "vehicle_lon": "-71.11936",
                                        "vehicle_timestamp": "1414721078"
                                    }
                                },
                                {
                                    "trip_headsign": "Alewife",
                                    "trip_id": "23850873",
                                    "trip_name": "9:46 pm from Ashmont - Inbound to Alewife",
                                    "vehicle": {
                                        "vehicle_bearing": "310",
                                        "vehicle_id": "1734",
                                        "vehicle_lat": "42.3565",
                                        "vehicle_lon": "-71.06263",
                                        "vehicle_timestamp": "1414721049"
                                    }
                                },
                                {
                                    "trip_headsign": "Alewife",
                                    "trip_id": "23850843",
                                    "trip_name": "9:58 pm from Ashmont - Inbound to Alewife",
                                    "vehicle": {
                                        "vehicle_bearing": "15",
                                        "vehicle_id": "1854",
                                        "vehicle_lat": "42.31054",
                                        "vehicle_lon": "-71.05362",
                                        "vehicle_timestamp": "1414721045"
                                    }
                                }
                            ]
                        }
                    ],
                    "route_id": "931_",
                    "route_name": "Red Line"
                }
            ],
            "route_type": "1"
        }
    ]
}
    """

PREDICTIONS = """{"mode":[{"route_type":"1","mode_name":"Subway","route":[{"route_id":"931_","route_name":"Red Line","direction":[{"direction_id":"0","direction_name":"Southbound","trip":[{"trip_id":"23850842","trip_name":"12:22 am from Alewife to Ashmont - Outbound","trip_headsign":"Ashmont","vehicle":{"vehicle_id":"1507","vehicle_lat":"42.35648","vehicle_lon":"-71.06265","vehicle_bearing":"130","vehicle_timestamp":"1414731453"},"stop":[{"stop_sequence":"10","stop_id":"70079","stop_name":"South Station - Outbound","sch_arr_dt":"1414730580","sch_dep_dt":"1414730580","pre_dt":"1414731608","pre_away":"65"},{"stop_sequence":"11","stop_id":"70081","stop_name":"Broadway - Outbound","sch_arr_dt":"1414730700","sch_dep_dt":"1414730700","pre_dt":"1414731741","pre_away":"198"},{"stop_sequence":"12","stop_id":"70083","stop_name":"Andrew - Outbound","sch_arr_dt":"1414730820","sch_dep_dt":"1414730820","pre_dt":"1414731869","pre_away":"326"},{"stop_sequence":"13","stop_id":"70085","stop_name":"JFK/UMASS Ashmont - Outbound","sch_arr_dt":"1414730940","sch_dep_dt":"1414730940","pre_dt":"1414732010","pre_away":"467"},{"stop_sequence":"14","stop_id":"70087","stop_name":"Savin Hill - Outbound","sch_arr_dt":"1414731120","sch_dep_dt":"1414731120","pre_dt":"1414732153","pre_away":"610"},{"stop_sequence":"15","stop_id":"70089","stop_name":"Fields Corner - Outbound","sch_arr_dt":"1414731300","sch_dep_dt":"1414731300","pre_dt":"1414732328","pre_away":"785"},{"stop_sequence":"16","stop_id":"70091","stop_name":"Shawmut - Outbound","sch_arr_dt":"1414731420","sch_dep_dt":"1414731420","pre_dt":"1414732450","pre_away":"907"},{"stop_sequence":"17","stop_id":"70093","stop_name":"Ashmont - Outbound","sch_arr_dt":"1414731540","sch_dep_dt":"1414731540","pre_dt":"1414732605","pre_away":"1062"}]}]},{"direction_id":"1","direction_name":"Northbound","trip":[{"trip_id":"23850846","trip_name":"12:30 am from Ashmont - Inbound to Alewife","trip_headsign":"Alewife","vehicle":{"vehicle_id":"1809","vehicle_lat":"42.35526","vehicle_lon":"-71.06016","vehicle_bearing":"310","vehicle_timestamp":"1414731355"},"stop":[{"stop_sequence":"10","stop_id":"70076","stop_name":"Park Street - to Alewife","sch_arr_dt":"1414730940","sch_dep_dt":"1414730940","pre_dt":"1414731542","pre_away":"0"},{"stop_sequence":"11","stop_id":"70074","stop_name":"Charles/MGH - Outbound","sch_arr_dt":"1414731060","sch_dep_dt":"1414731060","pre_dt":"1414731666","pre_away":"123"},{"stop_sequence":"12","stop_id":"70072","stop_name":"Kendall/MIT - Outbound","sch_arr_dt":"1414731180","sch_dep_dt":"1414731180","pre_dt":"1414731807","pre_away":"264"},{"stop_sequence":"13","stop_id":"70070","stop_name":"Central - Outbound","sch_arr_dt":"1414731300","sch_dep_dt":"1414731300","pre_dt":"1414731955","pre_away":"412"},{"stop_sequence":"14","stop_id":"70068","stop_name":"Harvard - Outbound","sch_arr_dt":"1414731480","sch_dep_dt":"1414731480","pre_dt":"1414732150","pre_away":"607"},{"stop_sequence":"15","stop_id":"70066","stop_name":"Porter - Outbound","sch_arr_dt":"1414731660","sch_dep_dt":"1414731660","pre_dt":"1414732332","pre_away":"789"},{"stop_sequence":"16","stop_id":"70064","stop_name":"Davis - Outbound","sch_arr_dt":"1414731840","sch_dep_dt":"1414731840","pre_dt":"1414732454","pre_away":"911"},{"stop_sequence":"17","stop_id":"70061","stop_name":"Alewife","sch_arr_dt":"1414732080","sch_dep_dt":"1414732080","pre_dt":"1414732617","pre_away":"1074"}]}]}]}]}],"alert_headers":[]}"""

def make_field_map(obj, ret, name):
    if name not in ret:
        ret[name] = {}

    for key, value in obj.items():
        ret[name][key] = type(value)

    for key, value in obj.items():
        if isinstance(value, dict):
            make_field_map(value, ret, key)
        elif isinstance(value, list):
            for item in value:
                make_field_map(item, ret, key)
            else:
                make_field_map({}, ret, key)

def make_type_str(fieldmap, k, fieldname):
    type = fieldmap[k][fieldname]
    if type == int:
        return 'int'
    elif type in six.string_types:
        return 'String'
    elif type == six.text_type:
        return 'String'
    elif type == list:
        # skip, we only have fields for primitives
        return None
    elif type == dict:
        return None
    else:
        raise Exception("Unknown type %s" % type)
    

def make_fields(fieldmap):
    ret = ""
    for k, v in fieldmap.items():
        for fieldname, type in v.items():
            type_str = make_type_str(fieldmap, k, fieldname)
            if type_str:
                ret += "    protected " + type_str + " " + fieldname + ";\n"
    return ret

def make_overrides(fieldmap):
    ret = ""
    for k, v in fieldmap.items():
        ret += "    protected abstract void on" + k[0].upper() + k[1:] + "() throws IOException;\n"

    return ret

def make_methods(fieldmap):
    ret = ""

    for k, v in fieldmap.items():
        clearfields = ""
        for fieldname, type in v.items():
            if type == int:
                clearfields += "        this." + fieldname + " = 0;\n";
            elif isinstance(type, six.string_types) or type == six.text_type:
                clearfields += "        this." + fieldname + " = null;\n";
            elif type == list or type == dict:
                pass
            else:
                raise Exception("Unknown type %s" % type)

        cases = ""
        first = True
        for fieldname, type in v.items():
            if first:
                cases += "            if (name.equals(\"{fieldname}\")) {{\n".format(fieldname=fieldname)
            else:
                cases += "            else if (name.equals(\"{fieldname}\")) {{\n".format(fieldname=fieldname)
            if type == list:
                cases += "                reader.beginArray();\n"
                cases += "                while (reader.hasNext()) {\n"
                cases += "                    " + fieldname + "();\n"
                cases += "                }\n"
                cases += "                reader.endArray();\n"
            elif type == dict:
                cases += "                " + fieldname + "();\n"
            elif type == int:
                cases += "                this.{fieldname} = reader.nextInteger();\n".format(fieldname=fieldname)
            elif isinstance(type, six.string_types) or type == six.text_type:
                cases += "                this.{fieldname} = reader.nextString();\n".format(fieldname=fieldname)
            else:
                raise Exception("Unknown type %s" % type)
            cases += "            }\n"
            first = False
        if not first:
            cases += "            else {\n"
            cases += "                reader.skipValue();\n"
            cases += "            }\n"
        else:
            cases += "            reader.skipValue();\n"

        ret += """
    protected void {name}() throws IOException {{
{clearfields}
        this.reader.beginObject();

        while (reader.hasNext()) {{
            String name = reader.nextName();
{cases}
        }}
        on{uppername}();
        this.reader.endObject();
    }}
""".format(name=k, uppername=k[0].upper() + k[1:], cases=cases, clearfields=clearfields)
    return ret


def generate_json_reader(objects):
    fieldmap = {}
    for obj in objects:
        make_field_map(obj, fieldmap, "root")

    fields = make_fields(fieldmap)
    methods = make_methods(fieldmap)
    overrides = make_overrides(fieldmap)

    return """
package boston.Bus.Map.parser;

import com.google.gson.stream.JsonReader;

import java.io.IOException;

/**
 * AUTOGENERATED CLASS
 */
public abstract class MbtaRealtimeReader {{
    protected JsonReader reader;

{fields}

    public MbtaRealtimeReader(JsonReader jsonReader) {{
        this.reader = jsonReader;
    }}

    public void parse() throws IOException {{
        root();
    }}

{methods}

{overrides}

}}
""".format(fields=fields,
           methods=methods,
           overrides=overrides)
def main():
    print(generate_json_reader([json.loads(VEHICLE_LOCATIONS), json.loads(PREDICTIONS)]))

if __name__ == "__main__":
    main()
