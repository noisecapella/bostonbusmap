import math

def shortest_distance_to_segment(point, line):
    # from http://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line
    x1, y1 = line[0]
    x2, y2 = line[1]
    x0, y0 = point

    # scale longitude to match latitude for small distances
    # from http://en.wikipedia.org/wiki/Geographical_distance#Spherical_Earth_projected_to_a_plane
    lon_factor = math.cos((math.pi / 180.0) * x0)
    y1 *= lon_factor
    y2 *= lon_factor
    y0 *= lon_factor

    return abs((y2-y1)*x0 - (x2-x1)*y0 + x2*y1 - y2*x1) / math.sqrt((y2-y1)**2 + (x2-x1)**2)

def simplify_path(path):
    # adapted from http://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm
    epsilon = 0.00001

    dmax = 0
    index = 0
    end = len(path) - 1
    for i in range(1, end):
        d = shortest_distance_to_segment(path[i], (path[0], path[end]))
        if d > dmax:
            index = i
            dmax = d
    if dmax > epsilon:
        rec_results1 = simplify_path(path[:index + 1])
        rec_results2 = simplify_path(path[index:end + 1])

        ret = rec_results1[:len(rec_results1) - 1] + rec_results2
    else:
        ret = [path[0], path[end]]
    return ret
