from typing import Union

import os 
import sys
sys.path.append(os.path.join(os.path.dirname(__file__), '../../..'))
from common import *

LiteralValue = Union[str, bool, int, float, None]


class Transformation:
    owl_type = tb.Transformation

    def __init__(self, query: str, language: str = 'SPARQL') -> None:
        super().__init__()
        self.language = language
        self.query = query

    def triples(self):
        return [
            (tb.transformation_query, Literal(self.query)),
            (tb.transformation_language, Literal(self.language))
        ]


class CopyTransformation(Transformation):
    owl_type = tb.CopyTransformation

    def __init__(self, input: int, output: int) -> None:
        super().__init__(query=f'COPY input {input} TO output {output}', language='COPY')
        self.input = input
        self.output = output

    def triples(self):
        return super().triples() + [
            (tb.copy_input, Literal(self.input)),
            (tb.copy_output, Literal(self.output))
        ]


class LoaderTransformation(Transformation):
    owl_type = tb.LoaderTransformation

    def __init__(self) -> None:
        super().__init__(query=f'Set dataset AS output ', language='LOADER')
