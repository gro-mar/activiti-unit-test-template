/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.activiti.examples.flowable6.designbydoing;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

import java.util.ArrayList;
import java.util.List;

public class TestActivitiEntityEventListener implements ActivitiEventListener {

  private List<Event> eventsReceived;
  private Class<?> entityClass;

  public TestActivitiEntityEventListener(Class<?> entityClass) {
    this.entityClass = entityClass;

    eventsReceived = new ArrayList<>();
  }

  public void clearEvents() {
    this.eventsReceived = new ArrayList<>();
  }
  public List<Event> getEventsReceived() {
    return eventsReceived;
  }

  @Override
  public void onEvent(ActivitiEvent event) {
    if (event instanceof ActivitiEntityEvent && entityClass.isAssignableFrom(((ActivitiEntityEvent) event).getEntity().getClass())) {
      ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
      if (entityEvent.getType().equals(ActivitiEventType.TASK_CREATED)) {
        TaskEntity task = (TaskEntity) entityEvent.getEntity();
        eventsReceived.add(new Event(Event.Type.CREATED, task.getId(), task.getAssignee(), task.getName()));
      } else if (entityEvent.getType().equals(ActivitiEventType.ENTITY_UPDATED)) {
        TaskEntity task = (TaskEntity) entityEvent.getEntity();
        eventsReceived.add(new Event(Event.Type.UPDATED, task.getId(), task.getAssignee(), task.getName()));
      }
    }
  }

  @Override
  public boolean isFailOnException() {
    return true;
  }

}
