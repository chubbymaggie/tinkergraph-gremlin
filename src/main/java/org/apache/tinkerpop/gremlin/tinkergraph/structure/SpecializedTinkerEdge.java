/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.tinkergraph.structure;

import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.*;

public abstract class SpecializedTinkerEdge extends TinkerEdge {

    private final Set<String> specificKeys;
    // TODO: replace with proper global cache
    private final HashMap<String, Property> cachedProperties = new HashMap<>();

    protected SpecializedTinkerEdge(Object id, Vertex outVertex, String label, Vertex inVertex, Set<String> specificKeys) {
        super(id, outVertex, label, inVertex);
        this.specificKeys = specificKeys;
    }

    @Override
    public Set<String> keys() {
        return specificKeys;
    }

    @Override
    public <V> Property<V> property(String key) {
        if (cachedProperties.containsKey(key)) {
            // System.out.println("cache hit");
            return cachedProperties.get(key);
        } else {
            // System.out.println("no cache hit :(");
            Optional<V> value = specificProperty(key);
            Property<V> ret;
            if (value.isPresent()) {
                ret = new TinkerProperty<V>(this, key, value.get());
            } else {
                ret = Property.empty();
            }
            cachedProperties.put(key, ret);
            return ret;
        }
    }

    protected abstract <V> Optional<V> specificProperty(String key);

    @Override
    public <V> Iterator<Property<V>> properties(String... propertyKeys) {
        if (propertyKeys.length == 0) {
            return (Iterator) specificKeys.stream().map(key -> property(key)).iterator();
        } else {
            Set<String> keys = new HashSet<>(Arrays.asList(propertyKeys));
            keys.retainAll(specificKeys);
            return (Iterator) keys.stream().map(key -> property(key)).iterator();
        }
    }

    @Override
    public <V> Property<V> property(String key, V value) {
        cachedProperties.remove(key);
        return updateSpecificProperty(key, value);
    }

    protected abstract <V> Property<V> updateSpecificProperty(String key, V value);

}
