package com.streamwork.ch03.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinFreemarker;

class WebServer {

  static class Node extends HashMap<String, String> {
    public Node(String name, int parallelism) {
      super();

      this.put("name", name);
      this.put("parallelism", String.valueOf(parallelism));
    }
  }

  static class Edge extends HashMap<String, String> {
    public Edge(Node from, Node to) {
        super();

        this.put("from", from.get("name"));
        this.put("to", to.get("name"));
        this.put("from_parallelism", from.get("parallelism"));
        this.put("to_parallelism", to.get("parallelism"));
    }
  }

  private final String jobName;
  private final List<Node> sources = new ArrayList<>();
  private final List<Node> operators = new ArrayList<>();
  private final List<Edge> edges = new ArrayList<>();

  public WebServer(final String jobName, final List<Connection> connectionList) {
    this.jobName = jobName;
    Map<Node, Integer> incomingCountMap = new HashMap<>();
    for (Connection connection: connectionList) {
      Node from = new Node(connection.from().getComponent().getName(), connection.from().getComponent().getParallelism());
      Node to = new Node(connection.to().getComponent().getName(), connection.to().getComponent().getParallelism());

      Integer count = incomingCountMap.getOrDefault(to, 0);
      incomingCountMap.put(from, count);
      count = incomingCountMap.getOrDefault(to, 0);
      incomingCountMap.put(to, count + 1);

      edges.add(new Edge(from, to));
    }
    for (Node node: incomingCountMap.keySet()) {
      if (incomingCountMap.get(node) == 0) {
        sources.add(node);
      } else {
        operators.add(node);
      }
    }
  }

  public void start() {
    Configuration freemarkerCfg = new Configuration(Configuration.VERSION_2_3_31);
    freemarkerCfg.setTemplateLoader(new ClassTemplateLoader(WebServer.class, "/"));
    Javalin app = Javalin.create(config -> {
              config.staticFiles.add("/public", Location.CLASSPATH);
              config.fileRenderer(new JavalinFreemarker(freemarkerCfg));
            })
        .start(7000);
    app.get("/", this::indexHandler);
    app.get("/plan.json", this::planHandler);
  }

  private void indexHandler(Context ctx) {
    StringBuilder graph = new StringBuilder();
    for (Edge edge : edges) {
      String from = edge.get("from");
      String to = edge.get("to");
      graph.append(String.format(
        "%s(%s x%s) --> %s(%s x%s)\n",
        from.replaceAll("\\s",""),
        from,
        edge.get("from_parallelism"),
        to.replaceAll("\\s",""),
        to,
        edge.get("to_parallelism")
      ));
    }
    ctx.render("index.ftl", Map.of("job", jobName, "graph", graph));
  }

  private void planHandler(Context ctx) {
    Map<String, Object> plan = new HashMap<>();
    plan.put("name", jobName);
    plan.put("sources", sources);
    plan.put("operators", operators);
    plan.put("edges", edges);

    ctx.json(plan);
  }
}
