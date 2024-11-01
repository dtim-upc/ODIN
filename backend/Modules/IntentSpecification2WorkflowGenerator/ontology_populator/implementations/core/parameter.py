from typing import Union

from common import *

LiteralValue = Union[str, bool, int, float, None]


class Parameter:
    def __init__(self, label: str, datatype: URIRef, default_value: Union[URIRef, LiteralValue],
                 condition: str = '') -> None:
        super().__init__()
        self.label = label
        self.datatype = datatype
        self.default_value = default_value
        self.condition = condition

        self.url_name = self.label.replace(' ', '_').replace('-', '_').lower()

        self.uri_ref = None
