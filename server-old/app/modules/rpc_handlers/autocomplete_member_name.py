import unicodedata
from ..redis import redis_client
from ..models import rpc_pb2

def normalize_caseless(text):
    return unicodedata.normalize("NFKD", text.casefold())


def handle(self, request):
    response = rpc_pb2.RpcResponse()
    filter_str = normalize_caseless(request.autocompleteMemberNameRequest.partialName)

    participants = redis_client.lrange('participant_names', 0, -1)
    matching = [x for x in participants if filter_str in normalize_caseless(x.decode('utf-8'))]

    response.autocompleteMemberNameResponse.names.extend(matching)
    return response