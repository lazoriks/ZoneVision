# MSc Interactive Media Prep Plan — FH OÖ Hagenberg (July–Sept 2026)

**Start date:** July 7, 2026 · **Program starts:** October 2026 · **Duration:** 12 weeks

Background: 9+ years Java/Spring Boot/Kafka/Kubernetes/AWS-GCP, basic Python, no formal ML/CV, building **ZoneVision** (YOLOv8 + Spring Boot + OpenTelemetry).

Every week table uses the same Java-brain-to-new-domain mapping style so you always have an anchor to reason from.

---

## End-Goal Checklist (review weekly)

- [ ] Working ZoneVision demo: camera → YOLOv8 → Kafka → Spring Boot zone logic → Dynatrace dashboard → React UI
- [ ] Can derive and explain backpropagation math (chain rule through a 2-layer net) for the AI module
- [ ] German A1 — can handle daily-life situations (shopping, Anmeldung, small talk, university admin)
- [ ] Draft 1-page Master thesis research question, tied to ZoneVision
- [ ] Bonus: working knowledge of rasterization/ray-tracing pipeline (Real Time Graphics) and Paxos/RAFT + Mongo/Redis/Grafana/ELK (Big Data)

## Weekly Time Budget (suggested)

| Track | Hours/week |
|---|---|
| Core month topic (Python/Math/NN → CV/OpenCV → HCI/React/writing) | 8–10 |
| ZoneVision coding exercise | 4–6 |
| German A1 (daily, 20–30 min) | ~2.5 |
| Bonus semester-prep track (Real Time Graphics / Big Data) | 1–2 |

---

## Month 1 (July): Python + Math + Neural Network Fundamentals
*Target modules: Artificial Intelligence, Real Time Graphics (bonus track)*

### Week 1 (Jul 7–13) — Python for a Java brain, environment setup

**Goal:** Get productive in Python without fighting the language; ZoneVision Python tooling ready.

**Tasks**
- [ ] Install Python 3.12, `venv`/`pip`, Jupyter, VS Code Python extension — think of `venv` as a lightweight per-project classpath
- [ ] Learn list comprehensions, duck typing, `dict`/`set`, f-strings — duck typing ≈ Java's structural use of interfaces, minus the compiler safety net
- [ ] NumPy basics: arrays, broadcasting, vectorized ops — NumPy arrays are like Java arrays that also do vector math without loops
- [ ] Compare GIL vs JVM threading model — explains why Python ML code leans on native (C/CUDA) libraries instead of raw threads
- [ ] Set up a `requirements.txt`/`pyproject.toml` for the future ZoneVision Python service — your Maven/Gradle equivalent

**Resources**
| Resource | Type | Link |
|---|---|---|
| freeCodeCamp — Python for Everybody / Full Course | YouTube | https://www.youtube.com/c/Freecodecamp |
| Real Python | Articles | https://realpython.com/ |
| NumPy Quickstart | Docs | https://numpy.org/doc/stable/user/quickstart.html |
| Official Python Tutorial | Docs | https://docs.python.org/3/tutorial/ |

**ZoneVision exercise:** Write a small Python script that parses a sample YOLOv8 JSON detection output and prints per-zone occupancy counts — the same shape of code you'll extend in Month 2.

**Bonus (Real Time Graphics):** Skim Scratchapixel's "3D Computer Graphics Primer" — get the 10,000-ft view of rasterization vs. ray tracing before the deep dives later. https://www.scratchapixel.com/

---

### Week 2 (Jul 14–20) — Linear algebra refresher

**Goal:** Build geometric intuition for vectors/matrices — the language every ML formula is written in.

**Tasks**
- [ ] Vectors, dot product, matrix multiplication, transpose — a matrix multiply is just many dot products batched, similar to how a batch REST endpoint replaces N single calls
- [ ] Norms (L1/L2) and why they show up as regularization terms later
- [ ] Eigenvalues/eigenvectors — intuition only, no need to hand-compute for large matrices
- [ ] Reimplement 2–3 matrix operations in NumPy and verify against manual computation
- [ ] Benchmark vectorized NumPy vs. pure Python loops — like comparing a Stream/vectorized operation to a manual for-loop in Java, but the gap is 50–100x here

**Resources**
| Resource | Type | Link |
|---|---|---|
| 3Blue1Brown — Essence of Linear Algebra | YouTube playlist | https://www.youtube.com/playlist?list=PLZHQObOWTQDPD3MizzM2xVFitgF8hE_ab |
| Khan Academy — Linear Algebra | Free course | https://www.khanacademy.org/math/linear-algebra |
| MIT 18.06 (Gilbert Strang) | Free OCW | https://ocw.mit.edu/courses/18-06-linear-algebra-spring-2010/ |

**ZoneVision exercise:** Implement matrix multiplication two ways (naive triple-loop vs. `np.dot`), benchmark like a JMH microbenchmark, and log the timing difference — this builds intuition for why frameworks push everything into vectorized/GPU ops.

**Bonus (Real Time Graphics):** Scratchapixel — rasterization pipeline stages (vertex → clipping → fragment). Mentally map to a Spring filter/interceptor chain: each stage transforms the "request" (vertex) before it reaches the "controller" (pixel shader).

---

### Week 3 (Jul 21–27) — Calculus, probability, and the first perceptron

**Goal:** Get comfortable with derivatives/chain rule and basic probability, then build the smallest possible "neural network."

**Tasks**
- [ ] Derivatives, partial derivatives, chain rule — this *is* backpropagation once you see it repeated through layers
- [ ] Probability basics: distributions, expectation, Bayes' theorem
- [ ] Implement Bayes' theorem manually on a toy example (e.g., spam classifier by hand)
- [ ] Implement a single perceptron forward pass in raw NumPy (weights, bias, activation) — no framework, like writing a POJO before you reach for Spring
- [ ] Implement logistic regression from scratch (gradient descent loop you write yourself)

**Resources**
| Resource | Type | Link |
|---|---|---|
| 3Blue1Brown — Essence of Calculus | YouTube playlist | https://www.youtube.com/playlist?list=PLZHQObOWTQDMsr9K-rj53DwVRMYO3t5Yr |
| StatQuest — Bayes' Theorem, Probability | YouTube | https://www.youtube.com/c/joshstarmer |
| Khan Academy — Probability & Statistics | Free course | https://www.khanacademy.org/math/statistics-probability |

**ZoneVision exercise:** Build a logistic-regression classifier (pure NumPy) that labels a bounding box as "violation / no violation" using toy features (box area, aspect ratio, distance to zone boundary) — a conceptual preview of ZoneVision's ML decision layer.

**Bonus (Real Time Graphics):** Scratchapixel — ray-sphere intersection and basic Whitted ray tracing. It's a satisfying, self-contained "hello world" of rendering.

---

### Week 4 (Jul 28–Aug 3) — Backpropagation deep dive + Bayesian networks

**Goal:** Actually derive and code backprop by hand; get the vocabulary of Bayesian networks for the AI module.

**Tasks**
- [ ] Derive backprop for a 2-layer network on paper (chain rule applied layer by layer)
- [ ] Build a tiny autograd engine (or follow Karpathy's "micrograd") to see backprop as a computation graph — a computation graph is conceptually close to a Spring `ApplicationContext` bean dependency graph, just differentiable
- [ ] Implement the same 2-layer network's backprop in NumPy without autograd, to confirm you understand every gradient
- [ ] Bayesian networks: DAGs, conditional independence, simple inference by hand — a DAG of conditional probabilities is structurally similar to a dependency-injection graph, but edges carry probability instead of object references
- [ ] Install PyTorch, run first tensor ops, compare to your manual NumPy version

**Resources**
| Resource | Type | Link |
|---|---|---|
| 3Blue1Brown — Neural Networks (ch.1–4, incl. backprop) | YouTube playlist | https://www.youtube.com/playlist?list=PLZHQObOWTQDNU6R1_67000Dx_ZCJB-3pi |
| Andrej Karpathy — "The spelled-out intro to neural networks and backpropagation: building micrograd" | YouTube | https://www.youtube.com/watch?v=VMj-3S1tku0 |
| CS231n course notes (backprop section) | Free notes | https://cs231n.github.io/ |
| Coursera — Probabilistic Graphical Models (Daphne Koller), audit free | Course | https://www.coursera.org/specializations/probabilistic-graphical-models |

**ZoneVision exercise:** Turn your manual backprop code into a tiny 2-layer classifier trained on toy zone-occupancy features, log the loss curve — this is your mental model for what YOLOv8's training loop is doing under the hood.

**Bonus (Real Time Graphics):** Watch lecture 1 of TU Wien's free "Rendering" course by Károly Zsolnai-Fehér on YouTube for the rasterization-vs-ray-tracing tradeoffs used in real engines.

---

## Month 2 (August): Computer Vision + OpenCV + ZoneVision MVP
*Target modules: Visual Computing, Big Data (bonus track), continued AI (CNNs)*

### Week 5 (Aug 4–10) — Digital image processing + OpenCV basics

**Goal:** Understand images as data and get fluent in OpenCV.

**Tasks**
- [ ] Images as arrays: pixels, channels, color spaces (RGB/HSV/grayscale) — think `BufferedImage`/byte arrays, but everything is now a NumPy tensor
- [ ] Convolution filters by hand: blur, Sobel edge detection, Canny — convolution is the same sliding-window idea a Kafka Streams windowed aggregation uses, applied to pixels instead of events
- [ ] Install and run OpenCV-Python; load/display/save images and video frames
- [ ] Port ZoneVision's Java frame-capture/preprocessing logic conceptually into a Python/OpenCV prototype
- [ ] Implement resize + grayscale + Canny edge detection pipeline on a sample frame

**Resources**
| Resource | Type | Link |
|---|---|---|
| OpenCV-Python official tutorials | Docs | https://docs.opencv.org/4.x/d6/d00/tutorial_py_root.html |
| freeCodeCamp — OpenCV Course (Full Tutorial with Python) | YouTube | https://www.youtube.com/watch?v=oXlwWbU8l2o |
| PyImageSearch | Free blog tutorials | https://pyimagesearch.com/ |

**ZoneVision exercise:** Write a Python/OpenCV script that preprocesses a sample ZoneVision camera frame (resize → grayscale → Canny) and compare the output/timing to your existing Java preprocessing step.

**Bonus (Big Data):** Skim MongoDB University's free M001 intro — you'll use Mongo to persist detection events later this month. https://learn.mongodb.com/

---

### Week 6 (Aug 11–17) — CNNs and object-detection architectures

**Goal:** Understand convolutional networks and where YOLOv8 fits architecturally.

**Tasks**
- [ ] Convolution operation, stride, padding, pooling, feature maps — a CNN's stacked conv layers are conceptually like a Spring pipeline of Controller → Service → Repository, each stage refining the "request" (image) into more abstract features
- [ ] Read CS231n's CNN notes end-to-end
- [ ] Study YOLO architecture at a conceptual level: backbone → neck → head, anchors vs. anchor-free
- [ ] Read Ultralytics' YOLOv8 docs/architecture overview
- [ ] Run YOLOv8 pretrained inference (CLI or Python) on sample ZoneVision footage, inspect the raw output tensor (boxes, classes, confidences)

**Resources**
| Resource | Type | Link |
|---|---|---|
| CS231n — Convolutional Neural Networks notes | Free notes | https://cs231n.github.io/convolutional-networks/ |
| Ultralytics YOLOv8 Docs | Docs | https://docs.ultralytics.com/ |
| Ultralytics YouTube channel | YouTube | https://www.youtube.com/@Ultralytics |
| StatQuest — CNNs Clearly Explained | YouTube | https://www.youtube.com/c/joshstarmer |

**ZoneVision exercise:** Run YOLOv8n inference on ZoneVision test footage, log detection confidence distributions, and document the architecture (backbone/neck/head) in the ZoneVision README.

**Bonus (Big Data):** Skim Redis University's free RU101 — you'll use Redis to cache current zone state. https://university.redis.com/

---

### Week 7 (Aug 18–24) — YOLOv8 ↔ Spring Boot integration (ZoneVision core)

**Goal:** Wire real-time detections into your existing Spring Boot service — the MVP's backbone.

**Tasks**
- [ ] Decide the integration path: Python microservice (REST/gRPC) exporting detections vs. running YOLOv8 natively in the JVM via Deep Java Library (DJL) + ONNX export
- [ ] If using ONNX: export YOLOv8 to ONNX and load it via DJL
- [ ] Implement zone-occupancy/crossing logic in Spring Boot consuming detection events
- [ ] Publish detection events to a Kafka topic (you already know this stack well) consumed by the zone-logic service
- [ ] Write JUnit tests for the zone-crossing logic; containerize the pipeline with Docker Compose

**Resources**
| Resource | Type | Link |
|---|---|---|
| Deep Java Library (DJL) Docs | Docs | https://djl.ai/ |
| Ultralytics — Export to ONNX guide | Docs | https://docs.ultralytics.com/modes/export/ |
| Spring Boot + Kafka reference docs | Docs | https://docs.spring.io/spring-kafka/reference/ |

**ZoneVision exercise:** Get an end-to-end path working: frame → YOLOv8 detection → Kafka event → Spring Boot zone-logic consumer → console/log output. This is the MVP milestone.

**Bonus (Big Data):** Add MongoDB persistence for detection/violation events (from Week 5's M001 basics).

---

### Week 8 (Aug 25–31) — Observability: OpenTelemetry + Dynatrace, Big Data wrap-up

**Goal:** Ship the "working ZoneVision demo with Dynatrace metrics" end goal.

**Tasks**
- [ ] Instrument the Spring Boot service with the OpenTelemetry Java SDK (traces, metrics, logs) — you already know Micrometer, OTel is the vendor-neutral evolution of that idea
- [ ] Configure an OTel Collector exporting to Dynatrace (or direct OTLP ingestion)
- [ ] Add custom metrics: detections/sec, zone-violation count, inference latency
- [ ] Build a Dynatrace dashboard visualizing these metrics live
- [ ] Skim Big Data module topics you don't already own: Grafana/ELK basics, and Paxos/RAFT consensus (interactive visualization) — you already know Kafka/K8s/distributed systems deeply, this just fills vocabulary gaps

**Resources**
| Resource | Type | Link |
|---|---|---|
| OpenTelemetry — Java Getting Started | Docs | https://opentelemetry.io/docs/languages/java/getting-started/ |
| Dynatrace — OpenTelemetry ingestion docs | Docs | https://docs.dynatrace.com/docs/ingest-from/opentelemetry |
| The Secret Lives of Data — Raft (interactive visualization) | Interactive | https://thesecretlivesofdata.com/raft/ |
| Grafana — Getting Started | Docs | https://grafana.com/docs/grafana/latest/getting-started/ |
| Elastic (ELK) — Getting Started | Docs | https://www.elastic.co/guide/en/elastic-stack/current/index.html |

**ZoneVision exercise:** Ship a Dynatrace dashboard showing live ZoneVision throughput, latency, and violation counts. ✅ This completes end-goal #1.

---

## Month 3 (September): HCI + React Advanced + Scientific Writing
*Target modules: Human Computer Interaction, Hypermedia Frameworks (bonus: reactive), thesis prep*

### Week 9 (Sep 1–7) — HCI foundations

**Goal:** Learn the core HCI vocabulary: Fitts's Law and design heuristics.

**Tasks**
- [ ] Fitts's Law: formula, index of difficulty, implications for target size/distance in UI design
- [ ] Norman's design principles: affordances, signifiers, feedback, mapping
- [ ] Nielsen's 10 usability heuristics — treat these like a linter's rule set for UI, the same way checkstyle enforces code conventions
- [ ] Do a heuristic evaluation of one existing app (any app you use daily)
- [ ] Sketch/wireframe the ZoneVision dashboard applying Fitts's Law to alert-acknowledgment buttons

**Resources**
| Resource | Type | Link |
|---|---|---|
| Nielsen Norman Group — Fitts's Law | Free articles | https://www.nngroup.com/articles/fittss-law/ |
| Nielsen Norman Group — 10 Usability Heuristics | Free article | https://www.nngroup.com/articles/ten-usability-heuristics/ |
| Interaction Design Foundation | Free articles | https://www.interaction-design.org/literature |

**ZoneVision exercise:** Produce a wireframe (paper or Figma free tier) of the ZoneVision alert dashboard, explicitly annotating Fitts's Law decisions (button size, proximity to likely cursor position).

---

### Week 10 (Sep 8–14) — Gestural/gaze interaction + empirical user studies

**Goal:** Learn interaction modalities beyond mouse/keyboard, and how to run a real user study.

**Tasks**
- [ ] Read overviews of gesture-based and gaze-based interaction (touch/mobile gestures, eye-tracking UIs)
- [ ] Study empirical methods: think-aloud protocol, A/B testing, System Usability Scale (SUS) — A/B testing maps directly onto feature-flag experiments you've likely run in production
- [ ] Design a small usability test plan for the ZoneVision dashboard wireframe
- [ ] Run the test with 3–5 people (friends/colleagues are fine), collect SUS scores
- [ ] Write up findings in a short report

**Resources**
| Resource | Type | Link |
|---|---|---|
| Interaction Design Foundation — Gestural/Touch Interaction | Free articles | https://www.interaction-design.org/literature/topics/gestural-interfaces |
| Nielsen Norman Group — Thinking Aloud / Usability Testing 101 | Free article | https://www.nngroup.com/articles/usability-testing-101/ |
| System Usability Scale (SUS) — usability.gov | Free reference | https://www.usability.gov/how-to-and-tools/methods/system-usability-scale.html |

**ZoneVision exercise:** Run the think-aloud usability test on your Week 9 wireframe, score it with SUS, and produce a 1-page findings report — this doubles as practice for the empirical-methods portion of the HCI module.

---

### Week 11 (Sep 15–21) — React advanced + reactive programming

**Goal:** Build the ZoneVision dashboard frontend and prep for Hypermedia Frameworks' reactive-programming content.

**Tasks**
- [ ] React: hooks (`useState`, `useEffect`, `useMemo`), component composition, data fetching patterns
- [ ] Build a dashboard page showing live zone occupancy + alert history from ZoneVision's API
- [ ] Add a WebSocket or SSE connection for live updates
- [ ] Learn Spring WebFlux basics: `Mono`/`Flux` — this maps closely to Kafka Streams backpressure concepts you already understand, just applied to HTTP
- [ ] Read Baeldung's Project Reactor intro for the JVM-side reactive story

**Resources**
| Resource | Type | Link |
|---|---|---|
| React official docs | Docs | https://react.dev/ |
| freeCodeCamp — React Course | YouTube | https://www.youtube.com/c/Freecodecamp |
| Spring WebFlux reference docs | Docs | https://docs.spring.io/spring-framework/reference/web/webflux.html |
| Baeldung — Project Reactor | Free tutorials | https://www.baeldung.com/reactor-core |

**ZoneVision exercise:** Ship the React dashboard consuming ZoneVision's live REST/WebSocket API — this is the UI half of end-goal #1's "working demo."

---

### Week 12 (Sep 22–28) — Scientific writing, thesis question, German review, final polish

**Goal:** Arrive at Hagenberg with a demo, a thesis direction, and enough German to function day to day.

**Tasks**
- [ ] Learn the structure of a research question/proposal (problem, gap, method, contribution)
- [ ] Draft a 1-page Master thesis research question tied to ZoneVision, e.g. *"Real-time zone-violation detection accuracy vs. latency trade-offs for edge-deployed YOLO models under observability-driven adaptive inference"* — adjust based on your actual findings from Month 2
- [ ] Consolidate German A1: daily-life vocabulary (Anmeldung, university admin, shopping, small talk); take a free A1 self-assessment
- [ ] Final end-to-end ZoneVision rehearsal: capture → YOLOv8 → Kafka → Spring Boot → Dynatrace → React UI
- [ ] Write/finish the ZoneVision README and record a short demo video

**Resources**
| Resource | Type | Link |
|---|---|---|
| Nicos Weg — A1 German course (Deutsche Welle) | Free course | https://learngerman.dw.com/en/nicos-weg/c-36519789 |
| Goethe-Institut — free placement test | Free test | https://www.goethe.de/en/spr/kup/prf/prf/prf1.html |
| Purdue OWL — Writing a Research Paper | Free guide | https://owl.purdue.edu/owl/research_and_citation/conducting_research/index.html |

**ZoneVision exercise:** Record a full demo run and finalize documentation — this closes out end-goals #1 and #4.

---

## Ongoing Weekly Habit (all 12 weeks)

- [ ] German A1 — 2–3 Nicos Weg episodes + 15 min spaced-repetition (Anki/Duolingo), daily
- [ ] 1 short journal entry per week: what confused you, what clicked — useful raw material for the thesis question and for orientation-week small talk

## Notes on Priority

If a week runs short on time, protect this order: **ZoneVision exercise > core month topic > German > bonus semester-prep track.** The bonus track (Real Time Graphics weeks 1–4, Big Data weeks 5–8) is a nice-to-have head start, not a blocker — both modules are taught from scratch in the program.
