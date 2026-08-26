# Running the code

This post trains the **same** logistic regression problem — *will the home team
win?* — four times, once each with [DeepNetts/JSR381](https://deepnetts.com),
[Tribuo](https://tribuo.org), [Weka](https://www.cs.waikato.ac.nz/ml/weka/), and
[Smile](https://haifengl.github.io/), entirely in Java, inside a Jupyter
notebook powered by the [JJava](https://github.com/dflib/jjava) kernel.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) with Compose v2
  (`docker compose`, not the legacy `docker-compose`)

That's it. No local Java, Maven, or Python installation is required — everything
runs in the container.

## Get the dataset

Unlike post 002, the data is **not** downloaded automatically: Kaggle requires a
login, so we can't fetch it unattended. Download the **International football
results from 1872 to 2017** dataset and unzip the CSVs into this post's `data/`
folder:

1. Open <https://www.kaggle.com/datasets/martj42/international-football-results-from-1872-to-2017>
2. Click **Download** (you'll need a free Kaggle account).
3. Unzip it so the CSVs land directly in `data/`:

```
003-logistic-regression-many-ways/
└── data/
    ├── results.csv        # the one we use
    ├── shootouts.csv
    ├── goalscorers.csv
    └── former_names.csv
```

Only `results.csv` is required for the notebook; the others come in the same zip.
The `data/` folder is git-ignored, so the CSVs stay out of the repo.

## Steps

From this directory (`posts/003-logistic-regression-many-ways/`):

```bash
docker compose up --build
```

The first build takes a few minutes (it installs the JDK and the JJava kernel).
When the logs settle, open:

- **JupyterLab:** [http://localhost:8888](http://localhost:8888)

Then open any notebook under `notebooks/` — the per-library deep-dives
(`smile.ipynb`, `tribuo.ipynb`, `weka.ipynb`, `deepnetts.ipynb`) or
`compare-all.ipynb` for all four side by side — and make sure the kernel
(top-right) is set to **Java**. Run the cells top to bottom.

To stop:

```bash
docker compose down
```

## Layout

```
003-logistic-regression-many-ways/
├── README.md            # the blog post
├── RUN.md               # this file
├── docker-compose.yml   # Jupyter + JJava (Java kernel), inline Dockerfile
├── notebooks/
│   ├── smile.ipynb          # one library per notebook (the deep-dives)
│   ├── tribuo.ipynb
│   ├── weka.ipynb
│   ├── deepnetts.ipynb
│   ├── compare-all.ipynb    # all four side by side, with an HTML dashboard
│   └── shared/              # the data pipeline every notebook %loads
└── data/                # you drop the Kaggle CSVs here (git-ignored)
```

## Notes

- The base image (`quay.io/jupyter/minimal-notebook`) includes Python only
  because Jupyter itself is a Python application. We are **not** configuring a
  Python data-science environment — all the ML code is Java.
- Each library's dependencies are resolved at runtime by the notebook's
  `%%loadFromPOM` cells, so the first run of those cells downloads them from
  Maven Central.
- Authentication is disabled (`--NotebookApp.token=''`) for local convenience.
  Do not expose this container to an untrusted network.
