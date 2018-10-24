package com.cibo.scalastan.ast

import com.cibo.scalastan._

class StanNodeSpec extends ScalaStanBaseSpec {

  private val v1 = StanLocalDeclaration[StanReal](StanReal(), "v1")
  private val v2 = StanLocalDeclaration[StanReal](StanReal(), "v2")
  private val v3 = StanLocalDeclaration[StanInt](StanInt(), "v3")
  private val y = StanLocalDeclaration[StanReal](StanReal(), "y")

  describe("StanContinuousDistribution") {
    it("generates sample syntax") {
      val model = new StanModel {
        y ~ stan.normal(v1, v2)
      }
      checkCode(model, "model { y ~ normal(v1,v2); }")
    }

    it("generates sample syntax with lower bound") {
      val model = new StanModel {
        y ~ stan.normal(v1, v2).truncate(v2)
      }
      checkCode(model, "model { y ~ normal(v1,v2) T[v2,]; }")
    }

    it("generates sample syntax with upper bound") {
      val model = new StanModel {
        y ~ stan.normal(v1, v2).truncate(upper = v2)
      }
      checkCode(model, "model { y ~ normal(v1,v2) T[,v2]; }")
    }

    it("generates sample syntax with upper and lower bounds") {
      val model = new StanModel {
        y ~ stan.normal(v1, v2).truncate(v1, v2)
      }
      checkCode(model, "model { y ~ normal(v1,v2) T[v1,v2]; }")
    }

    it("generates sample syntax with an expression on the LHS") {
      val model = new StanModel {
        stan.log(y) ~ stan.normal(v1, v2)
      }
      checkCode(model, "model { log(y) ~ normal(v1,v2); }")
    }

    it("generates lpdf syntax") {
      val model = new StanModel {
        target += stan.normal(v1, v2).lpdf(y)
      }
      checkCode(model, "model { target += normal_lpdf(y | v1,v2); }")
    }

    it("generates cdf syntax") {
      val model = new StanModel {
        v2 := stan.normal(v1, v2).cdf(y)
      }
      checkCode(model, "model { v2 = normal_cdf(y , v1,v2); }")
    }

    it("should allow rng in a generated quantity") {
      val model = new StanModel {
        val gen = new GeneratedQuantity(real()) {
          stan.normal(v1, v2).rng
        }
      }
    }

    it("should not allow rng in a model") {
      "val model = new Model { stan.normal(v1, v2).rng }" shouldNot compile
    }

    it("should allow rng with addition") {
      val model = new StanModel {
        val gen = new GeneratedQuantity(real()) {
          result := result + stan.normal(v1, v2).rng
        }
      }
    }
  }
}
