# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: user.proto

import sys
_b=sys.version_info[0]<3 and (lambda x:x) or (lambda x:x.encode('latin1'))
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
from google.protobuf import descriptor_pb2
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor.FileDescriptor(
  name='user.proto',
  package='bigsciencequiz',
  syntax='proto3',
  serialized_pb=_b('\n\nuser.proto\x12\x0e\x62igsciencequiz\"H\n\x04User\x12\n\n\x02id\x18\x01 \x01(\x05\x12\x13\n\x0bmemberNames\x18\x02 \x03(\t\x12\x10\n\x08teamName\x18\x03 \x01(\t\x12\r\n\x05score\x18\x04 \x01(\x05\x42!\n\x1f\x63lub.eslcc.bigsciencequiz.protob\x06proto3')
)
_sym_db.RegisterFileDescriptor(DESCRIPTOR)




_USER = _descriptor.Descriptor(
  name='User',
  full_name='bigsciencequiz.User',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='id', full_name='bigsciencequiz.User.id', index=0,
      number=1, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='memberNames', full_name='bigsciencequiz.User.memberNames', index=1,
      number=2, type=9, cpp_type=9, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='teamName', full_name='bigsciencequiz.User.teamName', index=2,
      number=3, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='score', full_name='bigsciencequiz.User.score', index=3,
      number=4, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=30,
  serialized_end=102,
)

DESCRIPTOR.message_types_by_name['User'] = _USER

User = _reflection.GeneratedProtocolMessageType('User', (_message.Message,), dict(
  DESCRIPTOR = _USER,
  __module__ = 'user_pb2'
  # @@protoc_insertion_point(class_scope:bigsciencequiz.User)
  ))
_sym_db.RegisterMessage(User)


DESCRIPTOR.has_options = True
DESCRIPTOR._options = _descriptor._ParseOptions(descriptor_pb2.FileOptions(), _b('\n\037club.eslcc.bigsciencequiz.proto'))
# @@protoc_insertion_point(module_scope)
