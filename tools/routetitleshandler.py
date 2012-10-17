import xml.sax
import xml.sax.handler

class RouteTitlesHandler(xml.sax.handler.ContentHandler):
    def __init__(self):
        self.mapping = {}

    def startElement(self, name, attributes):
        if name == "route":
            self.mapping[attributes["tag"]] = (attributes["title"], len(self.mapping))


