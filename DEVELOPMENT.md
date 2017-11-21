<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

This note is for the development and maintenance of the samples.
See [README.md](README.md) for using the samples.

At this time an ASF release is not created for the samples - 
neither a source release bundle nor binary sample artifacts are
released.  The expectation is that users will get the samples
from this github repository.

Generally, the desire is to have the samples build against the
latest released version of the runtime in Maven Central.

Hence when a new Edgent runtime is released, the samples build tooling
should be updated to reference the new Edgent release.  
E.g., when Edgent 1.3.0 is released:

```sh
    ./update-samples-version.sh --edgent-version 1.3.0
```

Review the changes and stage/commit the updated files.


Since we're not formally releasing the samples, it's currently
unclear when we'll decide to change the sample artifact version ids.
We may try to track maintain tracking the Edgent runtime's version.
Here's the easiest way to change the sample artifact version ids
when a decision is made to change them:

```sh
    ./update-samples-version.sh --samples-version 1.3.0-SNAPSHOT
```

Review the changes and stage/commit the updated files.
