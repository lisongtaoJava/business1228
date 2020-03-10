package com.atlisongtao.business1228.list;


import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@SpringBootTest
class BusinessListServiceApplicationTests {

	@Autowired
	private JestClient jestClient;

	@Test
	void contextLoads() {
	}

	@Test
	public  void testES() throws IOException {
		// dsl 语句
		String query="{\n" +
				"  \"query\": {\n" +
				"    \"match\": {\n" +
				"      \"actorList.name\": \"张译\"\n" +
				"    }\n" +
				"  }\n" +
				"}";
		Search search = new Search.Builder(query).addIndex("movie_chn").addType("movie").build();

		SearchResult searchResult = jestClient.execute(search);

		List<SearchResult.Hit<HashMap, Void>> hits = searchResult.getHits(HashMap.class);
		for (SearchResult.Hit<HashMap, Void> hit : hits) {
			HashMap map = hit.source;
			System.out.println(map.get("name"));
		}
	}
}

