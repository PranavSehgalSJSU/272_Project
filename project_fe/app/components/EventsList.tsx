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
      const response = await fetch('http://localhost:8080/api/events?limit=50');
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
          {events.map((event) => (
            <div key={event.id} className="border rounded-lg p-3 bg-gray-50">
              <div className="flex justify-between items-start">
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-medium">{event.type}</span>
                    <span className="text-xs text-gray-500">
                      {new Date(event.timestamp).toLocaleString()}
                    </span>
                  </div>
                  <p className="text-sm mt-1">{event.message}</p>
                  {event.ruleName && (
                    <p className="text-xs text-blue-600 mt-1">
                      Rule: {event.ruleName}
                    </p>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}