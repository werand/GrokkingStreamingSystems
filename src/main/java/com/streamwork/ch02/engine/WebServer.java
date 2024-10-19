package com.streamwork.ch02.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;

class WebServer {

  static class Node extends HashMap<String, String> {
    public Node(String name) {
      super();

      this.put("name", name);
    }
  }

  static class Edge extends HashMap<String, String> {
    public Edge(Node from, Node to) {
        super();

        this.put("from", from.get("name"));
        this.put("to", to.get("name"));
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
      Node from = new Node(connection.from.getComponent().getName());
      Node to = new Node(connection.to.getComponent().getName());

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
    Javalin app = Javalin.create(config -> config.addStaticFiles("/public", Location.CLASSPATH))
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
        "%s(%s) --> %s(%s)\n",
        from.replaceAll("\\s",""),
        from,
        to.replaceAll("\\s",""),
        to
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
