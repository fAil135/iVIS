Roles
=====

``(implementation of Role entity)``

Provides following method for `API <index.html>`_ calls:

    * `Get role`_
    * `Get roles`_
    * `Save role`_
    * `Save roles`_
    * `Update role`_
    * `Delete role`_
    * `Get role or roles by personal id`_
    * `Get role or roles by name`_

.. _`Get role`:

Get role
--------

URL:
~~~~
    */roles/{id}*

Method:
~~~~~~~
    *GET*

Parameters request:
~~~~~~~~~~~~~~~~~~~
    *null*

Parameters response:
~~~~~~~~~~~~~~~~~~~~
    *Object*

    *With properties:*
        #. id(NUMBER)
        #. name(STRING)

Example of response:
~~~~~~~~~~~~~~~~~~~~

.. code-block:: json

    {
      "id" : 0,
      "name" : ""
    }

.. _`Get roles`:

Get roles
---------

URL:
~~~~
    */roles*

Method:
~~~~~~~
    *GET*

Parameters request:
~~~~~~~~~~~~~~~~~~~
    *null*

Parameters response:
~~~~~~~~~~~~~~~~~~~~
    *Array*

.. seealso::
    
Array consists of objects from `Get role`_ method

Save role
---------

URL:
~~~~
    */roles*

Method:
~~~~~~~
    *POST*

Parameters request:
~~~~~~~~~~~~~~~~~~~
    *OBJECT(Role)*

Parameters response:
~~~~~~~~~~~~~~~~~~~~
    *OBJECT(Role)*

Null properties:
~~~~~~~~~~~~~~~~
    *id*

Save roles
----------

URL:
~~~~
    */roles*

Method:
~~~~~~~
    *POST*

Parameters request:
~~~~~~~~~~~~~~~~~~~
    *Array(Role)*

Parameters response:
~~~~~~~~~~~~~~~~~~~~
    *Array(Role)*
Null properties of every object in array:
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    *id*

.. _`Update role`:

Update role
-----------

URL:
~~~~
    */roles/{id}*

Method:
~~~~~~~
    *PUT*

Parameters request:
~~~~~~~~~~~~~~~~~~~
    *OBJECT(Role)*

Parameters response:
~~~~~~~~~~~~~~~~~~~~
    *OBJECT(Role)*

.. note::
    
property will be updated, if you don't want update property it need set null

.. _`Delete role`:

Delete role
-----------

URL:
~~~~
    */roles/{id}*

Method:
~~~~~~~
    *DELETE*

Parameters request:
~~~~~~~~~~~~~~~~~~~
    *null*

Parameters response:
~~~~~~~~~~~~~~~~~~~~
    *OBJECT(Role)*

.. note::
    you receive deleted object

.. _`Get role or roles by personal id`:

Get role or roles by personal id
-----------------------------

URL:
~~~~
    */roles

Method:
~~~~~~~
    *GET*

Parameters request:
~~~~~~~~~~~~~~~~~~~
    *personalId(STRING)*
    *first(BOOLEAN)* - optional

Parameters response:
~~~~~~~~~~~~~~~~~~~~
    *ARRAY or OBJECT (Role)*

.. _`Get role or roles by name`:

Get role or roles by name
-------------------------

URL:
~~~~
    */roles

Method:
~~~~~~~
    *GET*

Parameters request:
~~~~~~~~~~~~~~~~~~~
    *name(STRING)*
    *first(BOOLEAN)* - optional

Parameters response:
~~~~~~~~~~~~~~~~~~~~
    *ARRAY or OBJECT (Role)*

