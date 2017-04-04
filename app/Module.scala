import javax.inject.Named

import com.google.inject.{AbstractModule, Provides}
import model.{Order, OrderId}

import scala.collection.mutable

class Module extends AbstractModule {
  override def configure(): Unit = {}

  @Provides
  @Named("orderRepository")
  def orderRepository(): mutable.Map[OrderId, Order] = mutable.Map[OrderId, Order]()
}
