/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jtransc.ds;

import com.jtransc.annotation.JTranscInvisible;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.annotation.haxe.HaxeRemoveField;

import java.util.HashMap;

@JTranscInvisible
@HaxeAddMembers("var _map = new Map<Int, Dynamic>();")
public class FastIntMap<T> {
    @HaxeRemoveField
    private HashMap<Integer, T> map;

    @HaxeMethodBody("")
	@JTranscMethodBody(target = "js", value = "this.map = new Map();")
    public FastIntMap() {
        this.map = new HashMap<Integer, T>();
    }

    @HaxeMethodBody("return _map.get(p0);")
	@JTranscMethodBody(target = "js", value = "return this.map.get(p0);")
    public T get(int key) {
        return this.map.get(key);
    }

    @HaxeMethodBody("_map.set(p0, p1);")
	@JTranscMethodBody(target = "js", value = "this.map.set(p0, p1);")
    public void set(int key, T value) {
        this.map.put(key, value);
    }

    @HaxeMethodBody("return _map.exists(p0);")
	@JTranscMethodBody(target = "js", value = "return this.map.has(p0);")
    public boolean has(int key) {
        return this.map.containsKey(key);
    }

	@HaxeMethodBody("_map.remove(p0);")
	@JTranscMethodBody(target = "js", value = "this.map.delete(p0);")
	public void remove(int key) {
		this.map.remove(key);
	}

	//@HaxeMethodBody("_map.remove(p0);")
	//@JTranscMethodBody(target = "js", value = "return this.map.size;")
	//public int size() {
	//	return this.map.size();
	//}
}
