'use client';

import React, { useState, useEffect } from 'react';

export type Event = {
  id: string;
  timestamp: string;
  type: string;
  message: string;
  ruleId?: string;
  ruleName?: string;
};

type EventsListProps = {
  onRefresh?: () => void;
};

export function EventsList({ onRefresh }: EventsListProps) {
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchEvents = async () => {
    try {
      setLoading(true);
      
      // Get user context for filtering user-specific events
      const username = localStorage.getItem('username');
      const isAdmin = localStorage.getItem('isAdmin') === 'true';
      
      let url = 'http://localhost:8080/api/events?limit=50';
      
      // If not admin, filter events for this user
      if (!isAdmin && username) {
        url += `&userId=${encodeURIComponent(username)}`;
      }
      
      const response = await fetch(url);
      if (response.ok) {
        const data = await response.json();
        setEvents(data);
      }
    } catch (error) {
      console.error('Failed to fetch events:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEvents();
  }, []);

  useEffect(() => {
    if (onRefresh) {
      fetchEvents();
    }
  }, [onRefresh]);

  if (loading) {
    return <div className="text-center py-4">Loading events...</div>;
  }

  return (
    <div className="space-y-2">
      <div className="flex justify-between items-center">
        <h3 className="text-lg font-medium">Recent Events</h3>
        <button
          onClick={fetchEvents}
          className="px-3 py-1 text-sm bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          Refresh
        </button>
      </div>
      
      {events.length === 0 ? (
        <div className="text-center py-8 text-gray-500">
          No events found
        </div>
      ) : (
        <div className="space-y-2 max-h-96 overflow-y-auto">
          {events.map((event) => {
            const isUserEvent = event.type === 'USER_ALERT_RECEIVED';
            const bgColor = isUserEvent ? 'bg-blue-50 border-blue-200' : 'bg-gray-50';
            const typeColor = isUserEvent ? 'text-blue-600 bg-blue-100' : 'text-gray-600 bg-gray-100';
            
            return (
              <div key={event.id} className={`border rounded-lg p-3 ${bgColor}`}>
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <span className={`text-xs px-2 py-1 rounded-full font-medium ${typeColor}`}>
                        {isUserEvent ? '📧 My Alert' : '⚙️ System'}
                      </span>
                      <span className="text-xs text-gray-500">
                        {new Date(event.timestamp).toLocaleString()}
                      </span>
                    </div>
                    <p className="text-sm mt-2 font-medium">{event.message}</p>
                    {event.ruleName && (
                      <p className="text-xs text-blue-600 mt-1">
                        Emergency Rule: {event.ruleName}
                      </p>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}